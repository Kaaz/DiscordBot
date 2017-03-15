/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.main;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import emily.core.ExitCode;
import emily.db.controllers.CBotPlayingOn;
import emily.db.model.OBotPlayingOn;
import emily.handler.CommandHandler;
import emily.handler.GameHandler;
import emily.handler.MusicPlayerHandler;
import emily.handler.SecurityHandler;
import emily.handler.Template;
import emily.role.RoleRankings;
import emily.templates.TemplateCache;
import emily.templates.Templates;
import emily.threads.YoutubeThread;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Consumer;

/**
 * Shared information between bots
 */
public class BotContainer {
    public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
    private final int numShards;
    private final DiscordBot[] shards;
    private final YoutubeThread youtubeThread;
    private final AtomicBoolean statusLocked = new AtomicBoolean(false);
    private final AtomicInteger numGuilds;
    private final AtomicLongArray lastActions;
    private final ScheduledExecutorService scheduler;
    private volatile boolean allShardsReady = false;
    private volatile boolean terminationRequested = false;
    private volatile ExitCode rebootReason = ExitCode.UNKNOWN;


    public BotContainer(int numGuilds) throws LoginException, InterruptedException, RateLimitedException {
        scheduler = Executors.newScheduledThreadPool(3);
        this.numGuilds = new AtomicInteger(numGuilds);
        youtubeThread = new YoutubeThread();
        this.numShards = getRecommendedShards();
        shards = new DiscordBot[numShards];
        lastActions = new AtomicLongArray(numShards);
        initHandlers();
        initShards();
    }

    /**
     * restarts a shard
     *
     * @param shardId the shard to restart
     * @return true if it restarted
     */
    public synchronized boolean tryRestartingShard(int shardId) {
        try {
            restartShard(shardId);
        } catch (InterruptedException | LoginException | RateLimitedException e) {
            BotContainer.LOGGER.error("rebootshard failed", e);
            Launcher.logToDiscord(e, "shard-restart", "failed", "shard-id", shardId);
            return false;
        }
        return true;
    }

    /**
     * Schedule the a task somewhere in the future
     *
     * @param task     the task
     * @param delay    the delay
     * @param timeUnit unit type of delay
     */
    public void schedule(Runnable task, Long delay, TimeUnit timeUnit) {
        scheduler.schedule(task, delay, timeUnit);
    }

    /**
     * schedule a repeating task
     *
     * @param task        the taks
     * @param startDelay  delay before starting the first iteration
     * @param repeatDelay delay between consecutive executions
     */
    public ScheduledFuture<?> scheduleRepeat(Runnable task, long startDelay, long repeatDelay) {
        return scheduler.scheduleWithFixedDelay(task, startDelay, repeatDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * restarts a shard
     *
     * @param shardId the shard to restart
     * @throws InterruptedException
     * @throws LoginException
     * @throws RateLimitedException
     */
    public synchronized void restartShard(int shardId) throws InterruptedException, LoginException, RateLimitedException {
        for (Guild guild : shards[shardId].getJda().getGuilds()) {
            MusicPlayerHandler.removeGuild(guild, true);
        }
        System.out.println("shutting down shard " + shardId);
        shards[shardId].getJda().shutdown(false);
        System.out.println("SHUT DOWN SHARD " + shardId);
        schedule(() -> {
            while (true) {
                try {
                    shards[shardId].restartJDA();
                    break;
                } catch (LoginException | InterruptedException | RateLimitedException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(10_000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            List<OBotPlayingOn> radios = CBotPlayingOn.getAll();
            for (OBotPlayingOn radio : radios) {
                if (calcShardId(Long.parseLong(radio.guildId)) != shardId) {
                    continue;
                }
                Guild guild = shards[shardId].getJda().getGuildById(radio.guildId);
                if (guild != null) {
                    VoiceChannel channel = guild.getVoiceChannelById(radio.channelId);
                    if (channel != null) {
                        boolean hasUsers = false;
                        for (Member user : channel.getMembers()) {
                            if (!user.getUser().isBot()) {
                                hasUsers = true;
                                break;
                            }
                        }
                        if (hasUsers) {
                            MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, shards[shardId]);
                            player.connectTo(channel);
                            if (!player.isPlaying()) {
                                player.playRandomSong();
                            }
                        }
                    }
                }
                CBotPlayingOn.deleteGuild(radio.guildId);
            }
            reportError(String.format("Quick, shard `%02d` is on %s, where are the %s'? Restarting the shard, off we go %s!",
                    shardId, Emojibet.FIRE, Emojibet.FIRE_TRUCK, Emojibet.ROCKET));
        }, 5L, TimeUnit.SECONDS);

    }

    public void setLastAction(int shard, long timestamp) {
        lastActions.set(shard, timestamp);
    }

    public long getLastAction(int shard) {
        return lastActions.get(shard);
    }

    /**
     * Request that the bot exits
     *
     * @param reason the reason
     */
    public synchronized void requestExit(ExitCode reason) {
        if (!terminationRequested) {
            terminationRequested = true;
            rebootReason = reason;
            youtubeThread.shutown();
        }
    }

    /**
     *
     */
    public synchronized void firmRequestExit(ExitCode reason) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(300_000);// 5 minutes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(reason.getCode());
        }, "Firm-Request-Exit-Thread");
        thread.setDaemon(true);
        thread.start();
        requestExit(reason);
    }

    /**
     * report an error to the configured error channel
     *
     * @param error   the Exception
     * @param details extra details about the error
     */

    public void reportError(Throwable error, Object... details) {
        String errorMessage = "I've encountered a **" + error.getClass().getName() + "**" + Config.EOL;
        if (error.getMessage() != null) {
            errorMessage += "Message: " + Config.EOL;
            errorMessage += error.getMessage() + Config.EOL + Config.EOL;
        }
        String stack = "";
        int maxTrace = 10;
        StackTraceElement[] stackTrace1 = error.getStackTrace();
        for (int i = 0; i < stackTrace1.length; i++) {
            StackTraceElement stackTrace = stackTrace1[i];
            stack += stackTrace.toString() + Config.EOL;
            if (i > maxTrace) {
                break;
            }
        }
        if (details.length > 0) {
            errorMessage += "Extra information: " + Config.EOL;
            for (int i = 1; i < details.length; i += 2) {
                if (details[i] != null) {
                    errorMessage += details[i - 1] + " = " + details[i] + Config.EOL;
                } else if (details[i - 1] != null) {
                    errorMessage += details[i - 1];
                }
            }
            errorMessage += Config.EOL + Config.EOL;
        }
        errorMessage += "Accompanied stacktrace: " + Config.EOL + Misc.makeTable(stack) + Config.EOL;
        reportError(errorMessage);
    }

    public void reportError(String message) {
        DiscordBot shard = getShardFor(Config.BOT_GUILD_ID);
        Guild guild = shard.getJda().getGuildById(Config.BOT_GUILD_ID);
        if (guild == null) {
            LOGGER.warn("Can't find BOT_GUILD_ID " + Config.BOT_GUILD_ID);
            return;
        }
        TextChannel channel = guild.getTextChannelById(Config.BOT_ERROR_CHANNEL_ID);
        if (channel == null) {
            LOGGER.warn("Can't find BOT_ERROR_CHANNEL_ID " + Config.BOT_ERROR_CHANNEL_ID);
            return;
        }
        channel.sendMessage(message.length() > Config.MAX_MESSAGE_SIZE ? message.substring(0, Config.MAX_MESSAGE_SIZE - 1) : message).queue();
    }

    public void reportStatus(int shardId, JDA.Status oldStatus, JDA.Status status) {
        DiscordBot shard = getShardFor(Config.BOT_GUILD_ID);
        if (shard.getJda() == null) {
            return;
        }
        Guild guild = shard.getJda().getGuildById(Config.BOT_GUILD_ID);
        if (guild == null) {
            LOGGER.warn("Can't find BOT_GUILD_ID " + Config.BOT_GUILD_ID);
            return;
        }
        TextChannel channel = guild.getTextChannelById(Config.BOT_STATUS_CHANNEL_ID);
        if (channel == null) {
            LOGGER.warn("Can't find BOT_STATUS_CHANNEL_ID " + Config.BOT_STATUS_CHANNEL_ID);
            return;
        }
        if (channel.getJDA().getStatus() == JDA.Status.CONNECTED) {
            int length = 1 + (int) Math.floor(Math.log10(shards.length));
            channel.sendMessage(String.format(Emojibet.SHARD_ICON + " `%0" + length + "d/%0" + length + "d` | ~~%s~~ -> %s", shardId, shards.length, oldStatus.toString(), status.toString())).queue();
        }
    }

    /**
     * sends stats to discordlist.net
     */
    public void sendStatsToDiscordlistNet() {
        if (!Config.BOT_STATS_DISCORDLIST_NET || !allShardsReady()) {
            return;
        }
        int totGuilds = 0;
        for (DiscordBot shard : shards) {
            totGuilds += shard.getJda().getGuilds().size();
        }
        Unirest.post("https://bots.discordlist.net/api.php")
                .field("token", Config.BOT_STATS_DISCORDLIST_NET_TOKEN)
                .field("servers", totGuilds)
                .asStringAsync();
    }

    /**
     * update the numguilds so that we can check if we need an extra shard
     */
    public void guildJoined() {
        int suggestedShards = 1 + ((numGuilds.incrementAndGet() + 500) / 2000);
        if (suggestedShards > numShards) {
            terminationRequested = true;
            rebootReason = ExitCode.NEED_MORE_SHARDS;
        }
    }

    /**
     * Retrieves the shard recommendation from discord
     *
     * @return recommended shard count
     */
    public int getRecommendedShards() {
        try {
            HttpResponse<JsonNode> request = Unirest.get("https://discordapp.com/api/gateway/bot")
                    .header("Authorization", "Bot " + Config.BOT_TOKEN)
                    .header("Content-Type", "application/json")
                    .asJson();
            return Integer.parseInt(request.getBody().getObject().get("shards").toString());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * {@link BotContainer#guildJoined()}
     */
    public void guildLeft() {
        numGuilds.decrementAndGet();
    }

    public DiscordBot[] getShards() {
        return shards;
    }

    /**
     * {@link BotContainer#getShardFor(long)}
     */
    public DiscordBot getShardFor(String discordGuildId) {
        if (numShards == 1) {
            return shards[0];
        }
        return getShardFor(Long.parseLong(discordGuildId));
    }

    /**
     * Retrieves the right shard for the guildId
     *
     * @param discordGuildId the discord guild id
     * @return the instance responsible for the guild
     */
    public DiscordBot getShardFor(long discordGuildId) {
        if (numShards == 1) {
            return shards[0];
        }
        return shards[calcShardId(discordGuildId)];
    }

    /**
     * calculate to which shard the guild goes to
     *
     * @param discordGuildId discord guild id
     * @return shard number
     */
    public int calcShardId(long discordGuildId) {
        return (int) ((discordGuildId >> 22) % numShards);
    }

    /**
     * creates a new instance for each shard
     *
     * @throws LoginException       can't log in
     * @throws InterruptedException ¯\_(ツ)_/¯
     */
    private void initShards() throws LoginException, InterruptedException, RateLimitedException {
        for (int i = 0; i < shards.length; i++) {
            LOGGER.info("Starting shard #{} of {}", i, shards.length);
            shards[i] = new DiscordBot(i, shards.length, this);
            if (i == 0) {
                shards[i].initOnce();
            }
            Thread.sleep(5_000L);
        }
        for (DiscordBot shard : shards) {
            setLastAction(shard.getShardId(), System.currentTimeMillis());
        }
    }

    /**
     * After the bot is ready to go; reconnect to the voicechannels and start playing where it left off
     */
    private void onAllShardsReady() {
        TemplateCache.initGuildTemplates(this);
        System.out.println("DONE LOADING TEMPLATES");
        youtubeThread.start();
        CBotPlayingOn.deleteAll();
        sendStatsToDiscordlistNet();
    }

    private void initHandlers() {
        CommandHandler.initialize();
        GameHandler.initialize();
        SecurityHandler.initialize();
        Template.initialize();
        Templates.init();
        MusicPlayerHandler.init();
        RoleRankings.init();
    }

    /**
     * checks if all shards are ready
     *
     * @return all shards ready
     */
    public boolean allShardsReady() {
        if (allShardsReady) {
            return allShardsReady;
        }
        for (DiscordBot shard : shards) {
            if (shard == null || !shard.isReady()) {
                return false;
            }
        }
        allShardsReady = true;
        onAllShardsReady();
        return true;
    }

    public boolean isTerminationRequested() {
        return terminationRequested;
    }

    public ExitCode getRebootReason() {
        return rebootReason;
    }

    /**
     * Queue up a track to fetch from youtube
     *
     * @param youtubeCode the video code
     * @param message     message object
     * @param callback    the callback
     */
    public void downloadRequest(String youtubeCode, String youtubeTitle, Message message, Consumer<Message> callback) {
        youtubeThread.addToQueue(youtubeCode, youtubeTitle, message, callback);
    }

    /**
     * how many tracks are in the queue to be processed?
     *
     * @return amount
     */
    public int downloadsProcessing() {
        return youtubeThread.getQueueSize();
    }

    public synchronized boolean isInProgress(String videoCode) {
        return youtubeThread.isInProgress(videoCode);
    }

    /**
     * Check if the bot's status is locked
     * If its locked, the bot will not change its status
     *
     * @return locked?
     */
    public boolean isStatusLocked() {
        return statusLocked.get();
    }

    /**
     * Lock/unlock the bot's status
     *
     * @param locked lock?
     */
    public void setStatusLocked(boolean locked) {
        statusLocked.set(locked);
    }
}
