package discordbot.main;

import com.mashape.unirest.http.Unirest;
import discordbot.db.controllers.CBanks;
import discordbot.db.controllers.CGuild;
import discordbot.event.JDAEventManager;
import discordbot.event.JDAEvents;
import discordbot.guildsettings.bot.SettingActiveChannels;
import discordbot.guildsettings.bot.SettingAutoReplyModule;
import discordbot.guildsettings.bot.SettingBotChannel;
import discordbot.guildsettings.bot.SettingCleanupMessages;
import discordbot.guildsettings.bot.SettingEnableChatBot;
import discordbot.guildsettings.moderation.SettingCommandLoggingChannel;
import discordbot.guildsettings.moderation.SettingLoggingChannel;
import discordbot.guildsettings.moderation.SettingModlogChannel;
import discordbot.guildsettings.music.SettingMusicChannel;
import discordbot.handler.AutoReplyHandler;
import discordbot.handler.ChatBotHandler;
import discordbot.handler.CommandHandler;
import discordbot.handler.CommandReactionHandler;
import discordbot.handler.GameHandler;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.MusicReactionHandler;
import discordbot.handler.OutgoingContentHandler;
import discordbot.handler.SecurityHandler;
import discordbot.handler.Template;
import discordbot.role.RoleRankings;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DiscordBot {

	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	public final long startupTimeStamp;
	private final Map<String, String> defaultChannels = new ConcurrentHashMap<>();
	private final Map<String, String> musicChannels = new ConcurrentHashMap<>();
	private final Map<String, String> logChannels = new ConcurrentHashMap<>();
	private final int totShards;
	private final ScheduledExecutorService scheduler;
	public volatile JDA client;
	public String mentionMe;
	public String mentionMeAlias;
	public ChatBotHandler chatBotHandler = null;
	public SecurityHandler security = null;
	public OutgoingContentHandler out = null;
	public MusicReactionHandler musicReactionHandler = null;
	public CommandReactionHandler commandReactionHandler = null;
	private AutoReplyHandler autoReplyhandler;
	private GameHandler gameHandler = null;
	private volatile boolean isReady = false;
	private int shardId;
	private BotContainer container;
	private final JDAEventManager eventManager;

	public DiscordBot(int shardId, int numShards, BotContainer container) throws LoginException, InterruptedException, RateLimitedException {
		scheduler = Executors.newScheduledThreadPool(3);
		this.shardId = shardId;
		this.totShards = numShards;
		registerHandlers();
		setContainer(container);
		eventManager = new JDAEventManager(container);
		chatBotHandler = new ChatBotHandler();
		startupTimeStamp = System.currentTimeMillis() / 1000L;
		restartJDA();
		markReady();
		container.setLastAction(shardId, System.currentTimeMillis());
	}

	public void restartJDA() throws LoginException, InterruptedException, RateLimitedException {
		JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(Config.BOT_TOKEN);
		if (totShards > 1) {
			builder.useSharding(shardId, totShards);
		}
		builder.setEventManager(eventManager);
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setEnableShutdownHook(false);
		client = builder.buildBlocking();

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
		if (!logChannels.containsKey(guild.getId())) {
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
			logChannels.put(guild.getId(), channel.getId());
		}
		out.sendAsyncMessage(client.getTextChannelById(logChannels.get(guild.getId())), String.format("%s %s", category, message));
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
		if (!defaultChannels.containsKey(guild.getId())) {
			String channelIdentifier = GuildSettings.get(guild.getId()).getOrDefault(SettingBotChannel.class);
			TextChannel defaultChannel;
			if (channelIdentifier.matches("\\d{12,}")) {
				defaultChannel = guild.getTextChannelById(channelIdentifier);
			} else {
				defaultChannel = DisUtil.findChannel(guild, channelIdentifier);
			}
			if (defaultChannel == null || !defaultChannel.canTalk()) {
				defaultChannel = DisUtil.findFirstWriteableChannel(client, guild);
				if (defaultChannel == null) {
					return null;
				}
			}
			defaultChannels.put(guild.getId(), defaultChannel.getId());
		}
		return client.getTextChannelById(defaultChannels.get(defaultChannels.get(guild.getId())));
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
		Guild guild = client.getGuildById(guildId);
		if (!musicChannels.containsKey(guild.getId())) {
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
			if (channel == null) {
				return null;
			}
			musicChannels.put(guild.getId(), channel.getId());
		}
		return client.getTextChannelById(musicChannels.get(guild.getId()));
	}

	/**
	 * Retrieves the moderation log of a guild
	 *
	 * @param guildId the guild to get the modlog-channel for
	 * @return channel || null
	 */
	public synchronized TextChannel getModlogChannel(String guildId) {
		Guild guild = client.getGuildById(guildId);
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

	public synchronized void reconnect() {
		loadConfiguration();
	}

	/**
	 * Mark the shard as ready, the bot will start working once all shards are marked as ready
	 */
	public void markReady() {
		if (isReady) {
			return;
		}
		mentionMe = "<@" + this.client.getSelfUser().getId() + ">";
		mentionMeAlias = "<@!" + this.client.getSelfUser().getId() + ">";
		loadConfiguration();
		client.addEventListener(new JDAEvents(this));
		sendStatsToDiscordPw();
		isReady = true;
		RoleRankings.fixRoles(this.client.getGuilds());
		container.allShardsReady();
	}

	public synchronized void loadConfiguration() {
		defaultChannels.clear();
		musicChannels.clear();
		logChannels.clear();
		SecurityHandler.initialize();
	}

	public void reloadAutoReplies() {
		autoReplyhandler.reload();
	}

	/**
	 * Clears the cached channels for a guild
	 *
	 * @param guild the guild to clear for
	 */
	public synchronized void clearChannels(Guild guild) {
		defaultChannels.remove(guild.getId());
		musicChannels.remove(guild.getId());
		logChannels.remove(guild.getId());
	}

	public synchronized void clearChannels() {
		defaultChannels.clear();
		musicChannels.clear();
		logChannels.clear();
	}

	/**
	 * Remove all cached objects for a guild
	 *
	 * @param guild the guild to clear
	 */
	public void clearGuildData(Guild guild) {
		defaultChannels.remove(guild.getId());
		musicChannels.remove(guild.getId());
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
		return client.getSelfUser().getName();
	}

	public boolean setUserName(String newName) {
		if (!getUserName().equals(newName)) {
			client.getSelfUser().getManager().setName(newName).queue();
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
		if (author == null || author.isBot()) {
			return;
		}
		if (security.isBanned(author)) {
			return;
		}
		GuildSettings settings = GuildSettings.get(guild.getId());
		if (settings.getOrDefault(SettingActiveChannels.class).equals("mine") &&
				!channel.getName().equalsIgnoreCase(settings.getOrDefault(SettingBotChannel.class))) {
			if (message.getRawContent().equals(mentionMe + " reset yesimsure") || message.getRawContent().equals(mentionMeAlias + " reset yesimsure")) {
				channel.sendMessage(Emojibet.THUMBS_UP).queue();
				settings.set(null, SettingActiveChannels.class, "all");
			}
			return;
		}
		if (gameHandler.isGameInput(channel, author, message.getRawContent().toLowerCase())) {
			gameHandler.execute(author, channel, message.getRawContent(), null);
			return;
		}
		if (CommandHandler.isCommand(channel, message.getRawContent(), mentionMe, mentionMeAlias)) {
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
		data.put("server_count", client.getGuilds().size());
		if (totShards > 1) {
			data.put("shard_id", shardId);
			data.put("shard_count", totShards);
		}
		Unirest.post("https://bots.discord.pw/api/bots/" + client.getSelfUser().getId() + "/stats")
				.header("Authorization", Config.BOT_TOKEN_BOTS_DISCORD_PW)
				.header("Content-Type", "application/json")
				.body(data.toString())
				.asJsonAsync();
	}

	public void initOnce() {
		CBanks.init(client.getSelfUser().getId(), client.getSelfUser().getName());
	}
}