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
import emily.guildsettings.bot.SettingActiveChannels;
import emily.guildsettings.bot.SettingAutoReplyModule;
import emily.guildsettings.bot.SettingBotChannel;
import emily.guildsettings.bot.SettingCleanupMessages;
import emily.guildsettings.bot.SettingEnableChatBot;
import emily.guildsettings.moderation.SettingCommandLoggingChannel;
import emily.guildsettings.moderation.SettingLoggingChannel;
import emily.guildsettings.moderation.SettingModlogChannel;
import emily.guildsettings.music.SettingMusicChannel;
import emily.handler.AutoReplyHandler;
import emily.handler.ChatBotHandler;
import emily.handler.CommandHandler;
import emily.handler.CommandReactionHandler;
import emily.handler.GameHandler;
import emily.handler.GuildSettings;
import emily.handler.MusicPlayerHandler;
import emily.handler.MusicReactionHandler;
import emily.handler.OutgoingContentHandler;
import emily.handler.SecurityHandler;
import emily.handler.Template;
import emily.handler.discord.RestQueue;
import emily.role.RoleRankings;
import emily.util.DisUtil;
import emily.util.Emojibet;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordBot {

    public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
    public final long startupTimeStamp;
    private final int totShards;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<JDA> jda;
    public final RestQueue queue;
    public String mentionMe;
    public String mentionMeAlias;
    public ChatBotHandler chatBotHandler = null;
    public SecurityHandler security = null;
    public OutgoingContentHandler out = null;
    public MusicReactionHandler musicReactionHandler = null;
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
        chatBotHandler = new ChatBotHandler();
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

    public void updateJda(JDA jda) {
        this.jda.compareAndSet(this.jda.get(), jda);
    }

    public JDA getJda() {
        return jda.get();
    }

    public void restartJDA() throws LoginException, InterruptedException, RateLimitedException {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(Config.BOT_TOKEN);
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
        String cleanupMethod = GuildSettings.getFor(channel, SettingCleanupMessages.class);
        String myChannel = GuildSettings.getFor(channel, SettingBotChannel.class);
        if ("yes".equals(cleanupMethod)) {
            return true;
        } else if ("nonstandard".equals(cleanupMethod) && !channel.getName().equalsIgnoreCase(myChannel)) {
            return true;
        }
        return false;
    }

    public void logGuildEvent(Guild guild, String category, String message) {
        String channelIdentifier = GuildSettings.get(guild).getOrDefault(SettingLoggingChannel.class);
        if (channelIdentifier.equals("false")) {
            return;
        }
        TextChannel channel;
        if (channelIdentifier.matches("\\d{12,}")) {
            channel = guild.getTextChannelById(channelIdentifier);
        } else {
            channel = DisUtil.findChannel(guild, channelIdentifier);
        }
        if (channel == null || !channel.canTalk()) {
            GuildSettings.get(guild).set(guild, SettingLoggingChannel.class, "false");
            if (channel == null) {
                out.sendAsyncMessage(getDefaultChannel(guild), Template.get("guild_logchannel_not_found", channelIdentifier));
            } else {
                out.sendAsyncMessage(getDefaultChannel(guild), Template.get("guild_logchannel_no_permission", channelIdentifier));
            }
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
        String channelIdentifier = GuildSettings.get(guild.getId()).getOrDefault(SettingBotChannel.class);
        TextChannel defaultChannel;
        if (channelIdentifier.matches("\\d{12,}")) {
            defaultChannel = guild.getTextChannelById(channelIdentifier);
        } else {
            defaultChannel = DisUtil.findChannel(guild, channelIdentifier);
        }
        if (defaultChannel != null) {
            return defaultChannel;
        }
        return DisUtil.findFirstWriteableChannel(getJda(), guild);
    }

    /**
     * gets the default channel to output music to
     *
     * @param guild guild
     * @return default music channel
     */
    public synchronized TextChannel getMusicChannel(Guild guild) {
        return getMusicChannel(guild.getId());
    }

    public synchronized TextChannel getMusicChannel(String guildId) {
        Guild guild = getJda().getGuildById(guildId);
        if (guild == null) {
            return null;
        }
        String channelIdentifier = GuildSettings.get(guild.getId()).getOrDefault(SettingMusicChannel.class);
        TextChannel channel;
        if (channelIdentifier.matches("\\d{12,}")) {
            channel = guild.getTextChannelById(channelIdentifier);
        } else {
            channel = DisUtil.findChannel(guild, channelIdentifier);
        }

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
    public synchronized TextChannel getModlogChannel(String guildId) {
        Guild guild = getJda().getGuildById(guildId);
        String channelIdentifier = GuildSettings.get(guild.getId()).getOrDefault(SettingModlogChannel.class);
        if ("false".equals(channelIdentifier)) {
            return null;
        }
        return guild.getTextChannelById(channelIdentifier);
    }

    /**
     * Retrieves the moderation log of a guild
     *
     * @param guild the guild to get the modlog-channel for
     * @return channel || null
     */
    public synchronized TextChannel getCommandLogChannel(Guild guild) {
        String channelIdentifier = GuildSettings.get(guild.getId()).getOrDefault(SettingCommandLoggingChannel.class);
        if ("false".equals(channelIdentifier)) {
            return null;
        }
        return guild.getTextChannelById(channelIdentifier);
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
        GuildSettings.remove(guild.getId());
        Template.removeGuild(CGuild.getCachedId(guild.getId()));
        autoReplyhandler.removeGuild(guild.getId());
        MusicPlayerHandler.removeGuild(guild);
        commandReactionHandler.removeGuild(guild.getId());
    }

    /**
     * load data for a guild
     *
     * @param guild guild to load for
     */
    public void loadGuild(Guild guild) {
        int cachedId = CGuild.getCachedId(guild.getId());
        Template.initialize(cachedId);
        CommandHandler.loadCustomCommands(cachedId);
    }

    private void registerHandlers() {
        security = new SecurityHandler();
        gameHandler = new GameHandler(this);
        out = new OutgoingContentHandler(this);
        musicReactionHandler = new MusicReactionHandler(this);
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
        if (CommandHandler.isCommand(null, message.getRawContent(), mentionMe, mentionMeAlias)) {
            CommandHandler.process(this, channel, author, message.getRawContent());
        } else {
            channel.sendTyping();
            this.out.sendAsyncMessage(channel, this.chatBotHandler.chat("private", message.getRawContent()), null);
        }
    }

    public void handleMessage(Guild guild, TextChannel channel, User author, Message message) {
        if (author == null || (author.isBot() && !security.isInteractionBot(Long.parseLong(author.getId())))) {
            return;
        }
        if (security.isBanned(author)) {
            return;
        }
        GuildSettings settings = GuildSettings.get(guild.getId());
        if (settings.getOrDefault(SettingActiveChannels.class).equals("mine") &&
                !channel.getId().equals(settings.getOrDefault(SettingBotChannel.class))) {
            if (message.getRawContent().equals(mentionMe + " reset yesimsure") || message.getRawContent().equals(mentionMeAlias + " reset yesimsure")) {
                queue.add(channel.sendMessage(Emojibet.THUMBS_UP));
                settings.set(null, SettingActiveChannels.class, "all");
            }
            return;
        }
        if (gameHandler.isGameInput(channel, author, message.getRawContent().toLowerCase())) {
            gameHandler.execute(author, channel, message.getRawContent(), null);
            return;
        }
        if (CommandHandler.isCommand(channel, message.getRawContent().trim(), mentionMe, mentionMeAlias)) {
            CommandHandler.process(this, channel, author, message.getRawContent());
            return;
        }
        if (GuildSettings.getFor(channel, SettingAutoReplyModule.class).equals("true")) {
            if (autoReplyhandler.autoReplied(message)) {
                return;
            }
        }
        if (Config.BOT_CHATTING_ENABLED && settings.getOrDefault(SettingEnableChatBot.class).equals("true") &&
                channel.getId().equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class))) {
            if (PermissionUtil.checkPermission(channel, channel.getGuild().getSelfMember(), Permission.MESSAGE_WRITE)) {
                channel.sendTyping();
                this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(guild.getId(), message.getRawContent()), null);
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
        if (!Config.BOT_STATS_DISCORD_PW_ENABLED) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put("server_count", getJda().getGuilds().size());
        if (totShards > 1) {
            data.put("shard_id", shardId);
            data.put("shard_count", totShards);
        }
        Unirest.post("https://bots.discord.pw/api/bots/" + getJda().getSelfUser().getId() + "/stats")
                .header("Authorization", Config.BOT_TOKEN_BOTS_DISCORD_PW)
                .header("Content-Type", "application/json")
                .body(data.toString())
                .asJsonAsync();
    }

    public void sendStatsToDiscordbotsOrg() {
        if (Config.BOT_TOKEN_DISCORDBOTS_ORG.length() < 10) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put("server_count", getJda().getGuilds().size());
        if (totShards > 1) {
            data.put("shard_id", shardId);
            data.put("shard_count", totShards);
        }
        Unirest.post("https://discordbots.org/api/bots/" + getJda().getSelfUser().getId() + "/stats")
                .header("Authorization", Config.BOT_TOKEN_DISCORDBOTS_ORG)
                .header("Content-Type", "application/json")
                .body(data.toString())
                .asJsonAsync();
    }

    public void initOnce() {
        CBanks.init(getJda().getSelfUser().getId(), getJda().getSelfUser().getName());
    }
}