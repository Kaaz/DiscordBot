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

import com.mashape.unirest.http.Unirest;
import emily.db.controllers.CBanks;
import emily.db.controllers.CGuild;
import emily.event.JDAEventManager;
import emily.event.JDAEvents;
import emily.guildsettings.GSetting;
import emily.handler.*;
import emily.handler.discord.RestQueue;
import emily.role.RoleRankings;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Misc;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordBot {

    public static final Logger LOGGER = LogManager.getLogger(DiscordBot.class);
    public final long startupTimeStamp;
    public final RestQueue queue;
    private final int totShards;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<JDA> jda;
    public String mentionMe;
    public String mentionMeAlias;
    public ChatBotHandler chatBotHandler = null;
    public SecurityHandler security = null;
    public OutgoingContentHandler out = null;
    public MusicReactionHandler musicReactionHandler = null;
    public RoleReactionHandler roleReactionHandler = null;
    public CommandReactionHandler commandReactionHandler = null;
    public GameHandler gameHandler = null;
    private AutoReplyHandler autoReplyhandler;
    private volatile boolean isReady = false;
    private int shardId;
    private BotContainer container;

    public DiscordBot(int shardId, int numShards, BotContainer container) {
        queue = new RestQueue(this);
        scheduler = Executors.newScheduledThreadPool(1);
        jda = new AtomicReference<>();
        this.shardId = shardId;
        this.totShards = numShards;
        registerHandlers();
        setContainer(container);
        chatBotHandler = new ChatBotHandler(this);
        startupTimeStamp = System.currentTimeMillis() / 1000L;
        while (true) {
            try {
                restartJDA();
                break;
            } catch (LoginException | InterruptedException | RateLimitedException e) {
                try {
                    Thread.sleep(5_000L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        markReady();
        container.setLastAction(shardId, System.currentTimeMillis());
    }

    public Emote getEmote(String emoteString) {
        List<Emote> emotes = jda.get().getEmotesByName(emoteString, true);
        if (!emotes.isEmpty()) {
            return emotes.get(0);
        }

        if (Misc.parseLong(emoteString, 0) > 0) {
            return jda.get().getEmoteById(emoteString);
        }
        return null;
    }

    public void updateJda(JDA jda) {
        this.jda.compareAndSet(this.jda.get(), jda);
    }

    public JDA getJda() {
        return jda.get();
    }

    public void restartJDA() throws LoginException, InterruptedException, RateLimitedException {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(BotConfig.BOT_TOKEN);
        if (totShards > 1) {
            builder.useSharding(shardId, totShards);
        }
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setEnableShutdownHook(false);
        builder.setEventManager(new JDAEventManager(this));
        System.out.println("STARTING SHARD " + shardId);
        jda.set(builder.buildBlocking());
        jda.get().addEventListener(new JDAEvents(this));
        System.out.println("SHARD " + shardId + " IS READY ");
        //
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
     * Should the bot clean up after itself in specified channel?
     *
     * @param channel the channel to check for
     * @return delete the message?
     */
    public boolean shouldCleanUpMessages(MessageChannel channel) {
        String cleanupMethod = GuildSettings.getFor(channel, GSetting.CLEANUP_MESSAGES);
        String myChannel = GuildSettings.getFor(channel, GSetting.BOT_CHANNEL);
        if ("yes".equals(cleanupMethod)) {
            return true;
        } else if ("nonstandard".equals(cleanupMethod) && !channel.getName().equalsIgnoreCase(myChannel)) {
            return true;
        }
        return false;
    }

    public void logGuildEvent(Guild guild, String category, String message) {
        TextChannel channel = getChannelFor(guild.getIdLong(), GSetting.BOT_LOGGING_CHANNEL);
        if (channel == null) {
            return;
        }
        if (!channel.canTalk()) {
            out.sendAsyncMessage(getDefaultChannel(guild), Templates.config.cant_talk_in_channel.format(GuildSettings.get(guild).getOrDefault(GSetting.BOT_LOGGING_CHANNEL)));
            return;
        }
        out.sendAsyncMessage(channel, String.format("%s %s", category, message));
    }

    public int getShardId() {
        return shardId;
    }

    public boolean isReady() {
        return isReady;
    }

    /**
     * Gets the default channel to output to
     * if configured channel can't be found, return the first channel
     *
     * @param guild the guild to check
     * @return default chat channel
     */
    public synchronized TextChannel getDefaultChannel(Guild guild) {
        TextChannel defaultChannel = getChannelFor(guild.getIdLong(), GSetting.BOT_CHANNEL);
        if (defaultChannel != null) {
            return defaultChannel;
        }
        return DisUtil.findFirstWriteableChannel(guild);
    }

    /**
     * gets the default channel to output music to
     *
     * @param guild guild
     * @return default music channel
     */
    public synchronized TextChannel getMusicChannel(Guild guild) {
        return getMusicChannel(guild.getIdLong());
    }

    public synchronized TextChannel getMusicChannel(long guildId) {
        Guild guild = getJda().getGuildById(guildId);
        if (guild == null) {
            return null;
        }
        TextChannel channel = getChannelFor(guildId, GSetting.MUSIC_CHANNEL);
        if (channel == null) {
            channel = getDefaultChannel(guild);
        }
        if (channel == null || !channel.canTalk()) {
            return null;
        }
        return channel;
    }

    /**
     * Retrieves the moderation log of a guild
     *
     * @param guildId the guild to get the modlog-channel for
     * @return channel || null
     */
    public synchronized TextChannel getModlogChannel(long guildId) {
        return getChannelFor(guildId, GSetting.BOT_MODLOG_CHANNEL);
    }

    /**
     * retrieves a channel for setting
     *
     * @param guildId the guild
     * @param setting the channel setting
     * @return A text channel Or null in case it can't be found
     */
    private synchronized TextChannel getChannelFor(long guildId, GSetting setting) {
        Guild guild = getJda().getGuildById(guildId);
        if (guild == null) {
            return null;
        }
        String channelId = GuildSettings.get(guild.getIdLong()).getOrDefault(setting);
        if (channelId.matches("\\d{12,}")) {
            return guild.getTextChannelById(channelId);
        } else if (!channelId.isEmpty() && !"false".equals(channelId)) {
            return DisUtil.findChannel(guild, channelId);
        }
        return null;
    }

    /**
     * Retrieves the moderation log of a guild
     *
     * @param guild the guild to get the modlog-channel for
     * @return channel || null
     */
    public synchronized TextChannel getCommandLogChannel(long guild) {
        return getChannelFor(guild, GSetting.COMMAND_LOGGING_CHANNEL);
    }

    /**
     * Mark the shard as ready, the bot will start working once all shards are marked as ready
     */
    public void markReady() {
        if (isReady) {
            return;
        }
        mentionMe = "<@" + this.getJda().getSelfUser().getId() + ">";
        mentionMeAlias = "<@!" + this.getJda().getSelfUser().getId() + ">";
        sendStatsToDiscordPw();
        sendStatsToDiscordbotsOrg();
        isReady = true;
        RoleRankings.fixRoles(this.getJda().getGuilds());
        container.allShardsReady();
    }


    public void reloadAutoReplies() {
        autoReplyhandler.reload();
    }

    /**
     * Remove all cached objects for a guild
     *
     * @param guild the guild to clear
     */
    public void clearGuildData(Guild guild) {
        GuildSettings.remove(guild.getIdLong());
        autoReplyhandler.removeGuild(guild.getIdLong());
        MusicPlayerHandler.removeGuild(guild);
        commandReactionHandler.removeGuild(guild.getIdLong());
    }

    /**
     * load data for a guild
     *
     * @param guild guild to load for
     */
    public void loadGuild(Guild guild) {
        int cachedId = CGuild.getCachedId(guild.getIdLong());
        CommandHandler.loadCustomCommands(cachedId);
    }

    private void registerHandlers() {
        security = new SecurityHandler();
        gameHandler = new GameHandler(this);
        out = new OutgoingContentHandler(this);
        musicReactionHandler = new MusicReactionHandler(this);
        roleReactionHandler = new RoleReactionHandler(this);
        commandReactionHandler = new CommandReactionHandler();
        autoReplyhandler = new AutoReplyHandler(this);
    }

    public String getUserName() {
        return getJda().getSelfUser().getName();
    }

    public boolean setUserName(String newName) {
        if (!getUserName().equals(newName)) {
            getJda().getSelfUser().getManager().setName(newName).complete();
            return true;
        }
        return false;
    }

    public void addStreamToQueue(String url, Guild guild) {
        MusicPlayerHandler.getFor(guild, this).addStream(url);
        MusicPlayerHandler.getFor(guild, this).startPlaying();
    }

    public void handlePrivateMessage(PrivateChannel channel, User author, Message message) {
        if (security.isBanned(author)) {
            return;
        }
        if (CommandHandler.isCommand(null, message.getContentRaw(), mentionMe, mentionMeAlias)) {
            CommandHandler.process(this, channel, author, message);
        } else {
            channel.sendTyping().queue();
            this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(0L, message.getContentRaw(), channel), null);
        }
    }

    public void handleMessage(Guild guild, TextChannel channel, User author, Message message) {
        if (author == null || (author.isBot() && !security.isInteractionBot(author.getIdLong()))) {
            return;
        }
        if (security.isBanned(author)) {
            return;
        }
        GuildSettings settings = GuildSettings.get(guild.getIdLong());
        if (gameHandler.isGameInput(channel, author, message.getContentRaw().toLowerCase())) {
            gameHandler.execute(author, channel, message.getContentRaw(), null);
            return;
        }
        if (CommandHandler.isCommand(channel, message.getContentRaw().trim(), mentionMe, mentionMeAlias)) {
            CommandHandler.process(this, channel, author, message);
            return;
        }
        if (GuildSettings.getBoolFor(channel, GSetting.AUTO_REPLY)) {
            if (autoReplyhandler.autoReplied(message)) {
                return;
            }
        }
        if (BotConfig.BOT_CHATTING_ENABLED && settings.getBoolValue(GSetting.CHAT_BOT_ENABLED) &&
                channel.getId().equals(GuildSettings.get(channel.getGuild()).getOrDefault(GSetting.BOT_CHANNEL))) {
            if (PermissionUtil.checkPermission(channel, channel.getGuild().getSelfMember(), Permission.MESSAGE_WRITE)) {
                channel.sendTyping().queue();
                this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(guild.getIdLong(), message.getContentRaw(), channel), null);
            }
        }
    }

    public BotContainer getContainer() {
        return container;
    }

    public void setContainer(BotContainer container) {
        this.container = container;
    }

    public void sendStatsToDiscordPw() {
        if (!BotConfig.BOT_STATS_DISCORD_PW_ENABLED) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put("server_count", getJda().getGuilds().size());
        if (totShards > 1) {
            data.put("shard_id", shardId);
            data.put("shard_count", totShards);
        }
        Unirest.post("https://bots.discord.pw/api/bots/" + getJda().getSelfUser().getId() + "/stats")
                .header("Authorization", BotConfig.BOT_TOKEN_BOTS_DISCORD_PW)
                .header("Content-Type", "application/json")
                .body(data.toString())
                .asJsonAsync();
    }

    public void sendStatsToDiscordbotsOrg() {
        if (BotConfig.BOT_TOKEN_DISCORDBOTS_ORG.length() < 10) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put("server_count", getJda().getGuilds().size());
        if (totShards > 1) {
            data.put("shard_id", shardId);
            data.put("shard_count", totShards);
        }
        Unirest.post("https://discordbots.org/api/bots/" + getJda().getSelfUser().getId() + "/stats")
                .header("Authorization", BotConfig.BOT_TOKEN_DISCORDBOTS_ORG)
                .header("Content-Type", "application/json")
                .body(data.toString())
                .asJsonAsync();
    }

    public void initOnce() {
        CBanks.init(getJda().getSelfUser().getIdLong(), getJda().getSelfUser().getName());
    }
}