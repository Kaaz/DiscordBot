package discordbot.main;

import com.mashape.unirest.http.Unirest;
import discordbot.db.controllers.CGuild;
import discordbot.event.JDAEvents;
import discordbot.guildsettings.defaults.SettingActiveChannels;
import discordbot.guildsettings.defaults.SettingAutoReplyModule;
import discordbot.guildsettings.defaults.SettingBotChannel;
import discordbot.guildsettings.defaults.SettingCleanupMessages;
import discordbot.guildsettings.defaults.SettingEnableChatBot;
import discordbot.guildsettings.defaults.SettingLoggingChannel;
import discordbot.guildsettings.music.SettingMusicChannel;
import discordbot.handler.AutoReplyHandler;
import discordbot.handler.ChatBotHandler;
import discordbot.handler.CommandHandler;
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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
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
	private final Map<String, TextChannel> defaultChannels = new ConcurrentHashMap<>();
	private final Map<String, TextChannel> musicChannels = new ConcurrentHashMap<>();
	private final Map<String, TextChannel> logChannels = new ConcurrentHashMap<>();
	private final int totShards;
	private final ScheduledExecutorService scheduler;
	public volatile JDA client;
	public String mentionMe;
	public String mentionMeAlias;
	public ChatBotHandler chatBotHandler = null;
	public SecurityHandler security = null;
	public OutgoingContentHandler out = null;
	public MusicReactionHandler musicReactionHandler = null;
	private AutoReplyHandler autoReplyhandler;
	private GameHandler gameHandler = null;
	private volatile boolean isReady = false;
	private int shardId;
	private BotContainer container;

	public DiscordBot(int shardId, int numShards, BotContainer container) throws LoginException, InterruptedException, RateLimitedException {
		scheduler = Executors.newScheduledThreadPool(3);
		registerHandlers();
		JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(Config.BOT_TOKEN);
		this.shardId = shardId;
		this.totShards = numShards;
		if (numShards > 1) {
			builder.useSharding(shardId, numShards);
		}
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setEnableShutdownHook(false);
		client = builder.buildBlocking();
		startupTimeStamp = System.currentTimeMillis() / 1000L;
		setContainer(container);
		markReady();
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
		String channelName = GuildSettings.get(guild).getOrDefault(SettingLoggingChannel.class);
		if (channelName.equals("false")) {
			return;
		}
		if (!logChannels.containsKey(guild.getId())) {
			TextChannel channel = DisUtil.findChannel(guild, channelName);
			if (channel == null || !channel.canTalk()) {
				GuildSettings.get(guild).set(SettingLoggingChannel.class, "false");
				if (channel == null) {
					out.sendAsyncMessage(getDefaultChannel(guild), Template.get("guild_logchannel_not_found", channelName));
				} else {
					out.sendAsyncMessage(getDefaultChannel(guild), Template.get("guild_logchannel_no_permission", channelName));
				}
				return;
			}
			logChannels.put(guild.getId(), channel);
		}
		out.sendAsyncMessage(logChannels.get(guild.getId()), String.format("%s %s", category, message));
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
	public TextChannel getDefaultChannel(Guild guild) {
		if (!defaultChannels.containsKey(guild.getId())) {
			TextChannel defaultChannel = DisUtil.findChannel(guild, GuildSettings.get(guild).getOrDefault(SettingBotChannel.class));
			if (defaultChannel == null || !defaultChannel.canTalk()) {
				defaultChannel = DisUtil.findFirstWriteableChannel(client, guild);
			}
			defaultChannels.put(guild.getId(), defaultChannel);
		}
		return defaultChannels.get(guild.getId());
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
			TextChannel channel = DisUtil.findChannel(guild, GuildSettings.get(guild).getOrDefault(SettingMusicChannel.class));
			if (channel == null) {
				channel = getDefaultChannel(guild);
			}
			musicChannels.put(guild.getId(), channel);
		}
		return musicChannels.get(guild.getId());
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
		client.addEventListener(new JDAEvents(this));
		sendStatsToDiscordPw();
		isReady = true;
		loadConfiguration();
		mentionMe = "<@" + this.client.getSelfUser().getId() + ">";
		mentionMeAlias = "<@!" + this.client.getSelfUser().getId() + ">";
		RoleRankings.fixRoles(this.client.getGuilds());
		container.allShardsReady();
	}

	public void loadConfiguration() {
		defaultChannels.clear();
		musicChannels.clear();
		logChannels.clear();
		chatBotHandler = new ChatBotHandler();
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
		GuildSettings.remove(guild);
		Template.removeGuild(CGuild.getCachedId(guild.getId()));
		autoReplyhandler.removeGuild(guild.getId());
		MusicPlayerHandler.removeGuild(guild);
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
		if (CommandHandler.isCommand(null, message.getRawContent(), mentionMe, mentionMeAlias)) {
			CommandHandler.process(this, channel, author, message.getRawContent());
		} else {
			channel.sendTyping();
			this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(message.getRawContent()), null);
		}
	}

	public void handleMessage(Guild guild, TextChannel channel, User author, Message message) {
		if (author == null || author.isBot()) {
			return;
		}
		GuildSettings settings = GuildSettings.get(guild.getId());
		if (settings.getOrDefault(SettingActiveChannels.class).equals("mine") &&
				!channel.getName().equalsIgnoreCase(settings.getOrDefault(SettingBotChannel.class))) {
			if (message.getRawContent().equals(mentionMe + " reset yesimsure") || message.getRawContent().equals(mentionMeAlias + " reset yesimsure")) {
				channel.sendMessage(Emojibet.THUMBS_UP).queue();
				settings.set(SettingActiveChannels.class, "all");
			}
			return;
		}
		if (gameHandler.isGameInput(channel, author, message.getRawContent().toLowerCase())) {
			gameHandler.execute(author, channel, message.getRawContent());
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
				channel.getName().equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class))) {
			channel.sendTyping();
			this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(message.getRawContent()), null);
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
}