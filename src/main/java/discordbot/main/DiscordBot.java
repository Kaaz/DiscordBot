package discordbot.main;

import com.mashape.unirest.http.Unirest;
import discordbot.db.controllers.CGuild;
import discordbot.event.JDAEvents;
import discordbot.guildsettings.defaults.*;
import discordbot.guildsettings.music.SettingMusicChannel;
import discordbot.handler.*;
import discordbot.role.RoleRankings;
import discordbot.util.DisUtil;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordBot {

	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	public final long startupTimeStamp;
	private final Map<Guild, TextChannel> defaultChannels = new ConcurrentHashMap<>();
	private final Map<Guild, TextChannel> musicChannels = new ConcurrentHashMap<>();
	private final Map<Guild, TextChannel> logChannels = new ConcurrentHashMap<>();
	private final int totShards;
	public JDA client;
	public Timer timer = new Timer();
	public String mentionMe;
	public ChatBotHandler chatBotHandler = null;
	public SecurityHandler security = null;
	public OutgoingContentHandler out = null;
	public boolean statusLocked = false;
	private AutoReplyHandler autoReplyhandler;
	private GameHandler gameHandler = null;
	private volatile boolean isReady = false;
	private int shardId;
	private BotContainer container;

	public DiscordBot(int shardId, int numShards) throws LoginException, InterruptedException {
		registerHandlers();
		JDABuilder builder = new JDABuilder().setBotToken(Config.BOT_TOKEN);
		this.shardId = shardId;
		this.totShards = numShards;
		if (numShards > 1) {
			builder.useSharding(shardId, numShards);
		}
		builder.addListener(new JDAEvents(this));
		builder.setEnableShutdownHook(false);
		builder.setBulkDeleteSplittingEnabled(false);
		client = builder.buildAsync();
		startupTimeStamp = System.currentTimeMillis() / 1000L;
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
		} else if ("nonstandard".equals(cleanupMethod) && !((TextChannel) channel).getName().equalsIgnoreCase(myChannel)) {
			return true;
		}
		return false;
	}

	public void logGuildEvent(Guild guild, String catagory, String message) {
		String channelName = GuildSettings.get(guild).getOrDefault(SettingLoggingChannel.class);
		if (channelName.equals("false")) {
			return;
		}
		if (!logChannels.containsKey(guild)) {
			TextChannel channel = DisUtil.findChannel(guild, channelName);
			if (channel == null || !channel.checkPermission(client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
				GuildSettings.get(guild).set(SettingLoggingChannel.class, "false");
				if (channel == null) {
					out.sendAsyncMessage(getDefaultChannel(guild), Template.get("guild_logchannel_not_found", channelName));
				} else {
					out.sendAsyncMessage(getDefaultChannel(guild), Template.get("guild_logchannel_no_permission", channelName));
				}
				return;
			}
			logChannels.put(guild, channel);
		}
		out.sendAsyncMessage(logChannels.get(guild), String.format("\u231A`[%s]` %s %s", TimeUtil.timeFormat.format(new Date()), catagory, message));
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
		if (!defaultChannels.containsKey(guild)) {
			TextChannel defaultChannel = DisUtil.findChannel(guild, GuildSettings.get(guild).getOrDefault(SettingBotChannel.class));
			if (defaultChannel == null || !defaultChannel.checkPermission(client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
				defaultChannel = DisUtil.findFirstWriteableChannel(client, guild);
			}
			defaultChannels.put(guild, defaultChannel);
		}
		return defaultChannels.get(guild);
	}

	/**
	 * gets the default channel to output music to
	 *
	 * @param guild guild
	 * @return default music channel
	 */
	public TextChannel getMusicChannel(Guild guild) {
		if (!musicChannels.containsKey(guild)) {
			TextChannel channel = DisUtil.findChannel(guild, GuildSettings.get(guild).getOrDefault(SettingMusicChannel.class));
			if (channel == null) {
				channel = getDefaultChannel(guild);
			}
			musicChannels.put(guild, channel);
		}
		return musicChannels.get(guild);
	}

	/**
	 * Bot will start working once its marked ready
	 *
	 * @param ready ready to get started
	 */
	public void markReady(boolean ready) {
		loadConfiguration();
		mentionMe = "<@" + this.client.getSelfInfo().getId() + ">";
		RoleRankings.init();
		RoleRankings.fixRoles(this.client.getGuilds(), client);
		this.isReady = ready;
		sendStatsToDiscordPw();
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
	public void clearChannels(Guild guild) {
		defaultChannels.remove(guild);
		musicChannels.remove(guild);
		logChannels.remove(guild);
	}

	/**
	 * Remove all cached objects for a guild
	 *
	 * @param guild the guild to clear
	 */
	public void clearGuildData(Guild guild) {
		defaultChannels.remove(guild);
		musicChannels.remove(guild);
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
		timer = new Timer();
		autoReplyhandler = new AutoReplyHandler(this);
	}

	public String getUserName() {
		return client.getSelfInfo().getUsername();
	}

	public boolean setUserName(String newName) {
		if (isReady && !getUserName().equals(newName)) {
			client.getAccountManager().setUsername(newName).update();
			return true;
		}
		return false;
	}

	public void addStreamToQueue(String url, Guild guild) {
		MusicPlayerHandler.getFor(guild, this).addStream(url);
		MusicPlayerHandler.getFor(guild, this).startPlaying();

	}

	public void handlePrivateMessage(PrivateChannel channel, User author, Message message) {
		if (CommandHandler.isCommand(null, message.getRawContent(), mentionMe)) {
			CommandHandler.process(this, channel, author, message.getRawContent());
		} else {
			this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(message.getRawContent()), null);
		}
	}

	public void handleMessage(Guild guild, TextChannel channel, User author, Message message) {
		if (!isReady || author.isBot()) {
			return;
		}

		GuildSettings settings = GuildSettings.get(guild);
		if (settings.getOrDefault(SettingActiveChannels.class).equals("mine") &&
				!channel.getName().equalsIgnoreCase(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class))) {
			return;
		}
		if (gameHandler.isGameInput(channel, author, message.getRawContent().toLowerCase())) {
			gameHandler.execute(author, channel, message.getRawContent());
			return;
		}
		if (CommandHandler.isCommand(channel, message.getRawContent(), mentionMe)) {
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
			this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(message.getRawContent()), null);
		}
	}

	public float getVolume(Guild guild) {
		return MusicPlayerHandler.getFor(guild, this).getVolume();
	}

	public void connectTo(VoiceChannel channel) {
		MusicPlayerHandler.getFor(channel.getGuild(), this).connectTo(channel);
	}

	public boolean isConnectedTo(VoiceChannel channel) {
		return MusicPlayerHandler.getFor(channel.getGuild(), this).isConnectedTo(channel);
	}

	public boolean leaveVoice(Guild guild) {
		return MusicPlayerHandler.getFor(guild, this).leave();
	}

	public void setVolume(Guild guild, float volume) {
		MusicPlayerHandler.getFor(guild, this).setVolume(volume);
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
		Unirest.post("https://bots.discord.pw/api/bots/" + client.getSelfInfo().getId() + "/stats")
				.header("Authorization", Config.BOT_TOKEN_BOTS_DISCORD_PW)
				.header("Content-Type", "application/json")
				.body(data.toString())
				.asJsonAsync();
	}
}