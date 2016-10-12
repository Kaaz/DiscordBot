package discordbot.main;

import discordbot.db.model.OMusic;
import discordbot.event.JDAEvents;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.guildsettings.defaults.*;
import discordbot.handler.*;
import discordbot.role.RoleRankings;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordBot {

	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	public final long startupTimeStamp;
	public JDA client;
	public CommandHandler commands;
	public Timer timer = new Timer();
	public String mentionMe;
	public ChatBotHandler chatBotHandler = null;
	public OutgoingContentHandler out = null;
	public boolean statusLocked = false;
	private AutoReplyHandler autoReplyhandler;
	private GameHandler gameHandler = null;
	private boolean isReady = false;
	private Map<Guild, TextChannel> defaultChannels = new ConcurrentHashMap<>();
	private Map<Guild, TextChannel> musicChannels = new ConcurrentHashMap<>();

	public DiscordBot() throws LoginException, InterruptedException {
		registerHandlers();
		JDABuilder builder = new JDABuilder().setBotToken(Config.BOT_TOKEN);
		builder.addListener(new JDAEvents(this));
		client = builder.buildBlocking();
		startupTimeStamp = System.currentTimeMillis() / 1000L;
	}

	public boolean isReady() {
		return isReady;
	}

	/**
	 * Shortcut to check if a user is an administrator
	 *
	 * @param channel channel to check for
	 * @param user    the user to check
	 * @return is the user an admin?
	 */
	public boolean isAdmin(Channel channel, User user) {
		if (channel == null || channel instanceof PrivateChannel) {
			return false;
		}
		return isCreator(user) || channel.checkPermission(user, Permission.ADMINISTRATOR);
	}

	/**
	 * check if a user is the owner of a guild or isCreator
	 *
	 * @param channel the channel
	 * @param user    the user to check
	 * @return user is owner
	 */
	public boolean isOwner(Channel channel, User user) {
		if (channel instanceof PrivateChannel) {
			return isCreator(user);
		}
		return isCreator(user) || channel.getGuild().getOwner().equals(user);
	}

	/**
	 * checks if user is creator
	 *
	 * @param user user to check
	 * @return is creator?
	 */
	public boolean isCreator(User user) {
		return user.getId().equals(Config.CREATOR_ID);
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
			String channelName = GuildSettings.get(guild).getOrDefault(SettingBotChannel.class);
			List<TextChannel> channelList = guild.getTextChannels();
			boolean foundChannel = false;
			for (TextChannel channel : channelList) {
				if (channel.getName().equalsIgnoreCase(channelName)) {
					foundChannel = true;
					defaultChannels.put(guild, channel);
					break;
				}
			}

			if (!foundChannel) {
				TextChannel target = null;
				for (TextChannel channel : guild.getTextChannels()) {
					if (channel.checkPermission(client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
						target = channel;
						break;
					}
				}
				defaultChannels.put(guild, target);
			}
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
			String channelName = GuildSettings.get(guild).getOrDefault(SettingMusicChannel.class);
			List<TextChannel> channelList = guild.getTextChannels();
			boolean foundChannel = false;
			for (TextChannel channel : channelList) {
				if (channel.getName().equalsIgnoreCase(channelName)) {
					foundChannel = true;
					musicChannels.put(guild, channel);
					break;
				}
			}
			if (!foundChannel) {
				musicChannels.put(guild, getDefaultChannel(guild));
			}
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
		System.gc();
	}

	public void loadConfiguration() {
		commands.load();
		Template.getInstance().load();
		defaultChannels = new ConcurrentHashMap<>();
		musicChannels = new ConcurrentHashMap<>();
		chatBotHandler = new ChatBotHandler();
		autoReplyhandler.reload();
	}

	public void reloadGuild(Guild guild) {
		defaultChannels.remove(guild);
		musicChannels.remove(guild);
	}

	private void registerHandlers() {
		commands = new CommandHandler(this);
		gameHandler = new GameHandler(this);
		Template.setBot(this);
		out = new OutgoingContentHandler(this);
		timer = new Timer();
		autoReplyhandler = new AutoReplyHandler(this);
	}

	public String getUserName() {
		return client.getSelfInfo().getUsername();
	}

	public boolean setUserName(String newName) {
		if (isReady && !getUserName().equals(newName)) {
			client.getAccountManager().setUsername(newName);
			return true;
		}
		return false;
	}

	public void addSongToQueue(String filename, IGuild guild) {
		MusicPlayerHandler.getFor(guild, this).addToQueue(filename);
	}

	public void skipCurrentSong(IGuild guild) {
		MusicPlayerHandler.getFor(guild, this).skipSong();
	}

	public void setVolume(IGuild guild, float vol) {
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		player.setVolume(vol);
	}


	public void handlePrivateMessage(PrivateChannel channel, User author, Message message) {
		if (commands.isCommand(channel, message.getContent())) {
			commands.process(channel, author, message.getContent());
		} else {
			this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(message.getContent()), null);
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
		if (gameHandler.isGameInput(channel, author, message.getContent().toLowerCase())) {
			gameHandler.execute(author, channel, message.getContent());
			return;
		}
		if (commands.isCommand(channel, message.getContent())) {
			commands.process(channel, author, message.getContent());
			return;
		}
		if (GuildSettings.getFor(channel, SettingAutoReplyModule.class).equals("true")) {
			if (autoReplyhandler.autoReplied(message)) {
				return;
			}
		}
		if (Config.BOT_CHATTING_ENABLED && settings.getOrDefault(SettingEnableChatBot.class).equals("true") &&
				!DefaultGuildSettings.getDefault(SettingBotChannel.class).equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class)) &&
				channel.getName().equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class))) {
			this.out.sendAsyncMessage(channel, this.chatBotHandler.chat(message.getContent()), null);
		}
	}

	public float getVolume(IGuild guild) {
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		return player.getVolume();
	}

	public void trackEnded(AudioPlayer.Track oldTrack, Optional<AudioPlayer.Track> nextTrack, IGuild guild) {
		MusicPlayerHandler.getFor(guild, this).onTrackEnded(oldTrack, nextTrack);
	}

	public void trackStarted(AudioPlayer.Track track, IGuild guild) {
		MusicPlayerHandler.getFor(guild, this).onTrackStarted(track);
	}

	public void stopMusic(IGuild guild) {
		MusicPlayerHandler.getFor(guild, this).stopMusic();
	}

	public OMusic getCurrentlyPlayingSong(IGuild guild) {
		return MusicPlayerHandler.getFor(guild, this).getCurrentlyPlaying();
	}

	public List<IUser> getCurrentlyListening(IGuild guild) {
		return MusicPlayerHandler.getFor(guild, this).getUsersInVoiceChannel();
	}

	public boolean playRandomSong(IGuild guild) {
		return MusicPlayerHandler.getFor(guild, this).playRandomSong();
	}
}