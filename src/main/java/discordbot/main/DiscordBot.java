package discordbot.main;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OMusic;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.guildsettings.defaults.SettingActiveChannels;
import discordbot.guildsettings.defaults.SettingBotChannel;
import discordbot.guildsettings.defaults.SettingEnableChatBot;
import discordbot.guildsettings.defaults.SettingMusicChannel;
import discordbot.handler.*;
import discordbot.role.RoleRankings;
import discordbot.util.DisUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordBot {

	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	public final long startupTimeStamp;
	public IDiscordClient client;
	public CommandHandler commands;
	public Timer timer = new Timer();
	public String mentionMe;
	public ChatBotHandler chatBotHandler = null;
	public OutgoingContentHandler out = null;
	public boolean statusLocked = false;
	private GameHandler gameHandler = null;
	private boolean isReady = false;
	private Map<IGuild, IChannel> defaultChannels = new ConcurrentHashMap<>();
	private Map<IGuild, IChannel> musicChannels = new ConcurrentHashMap<>();

	public DiscordBot() throws DiscordException {
		registerHandlers();
		client = new ClientBuilder().withToken(Config.BOT_TOKEN).setMaxReconnectAttempts(128).login();
		registerEvents();
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
	public boolean isAdmin(IChannel channel, IUser user) {
		return isCreator(user) || (!channel.isPrivate() && DisUtil.hasPermission(user, channel.getGuild(), Permissions.ADMINISTRATOR));
	}

	/**
	 * check if a user is the owner of a guild or isCreator
	 *
	 * @param channel the channel
	 * @param user    the user to check
	 * @return user is owner
	 */
	public boolean isOwner(IChannel channel, IUser user) {
		if (channel.isPrivate()) {
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
	public boolean isCreator(IUser user) {
		return user.getID().equals(Config.CREATOR_ID);
	}

	/**
	 * Gets the default channel to output to
	 * if configured channel can't be found, return the first channel
	 *
	 * @param guild the guild to check
	 * @return default chat channel
	 */
	public IChannel getDefaultChannel(IGuild guild) {
		if (!defaultChannels.containsKey(guild)) {
			String channelName = GuildSettings.get(guild).getOrDefault(SettingBotChannel.class);
			List<IChannel> channelList = guild.getChannels();
			boolean foundChannel = false;
			for (IChannel channel : channelList) {
				if (channel.getName().equalsIgnoreCase(channelName)) {
					foundChannel = true;
					defaultChannels.put(guild, channel);
					break;
				}
			}
			if (!foundChannel) {
				defaultChannels.put(guild, channelList.get(0));
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
	public IChannel getMusicChannel(IGuild guild) {
		if (!musicChannels.containsKey(guild)) {
			String channelName = GuildSettings.get(guild).getOrDefault(SettingMusicChannel.class);
			List<IChannel> channelList = guild.getChannels();
			boolean foundChannel = false;
			for (IChannel channel : channelList) {
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
		mentionMe = "<@" + this.client.getOurUser().getID() + ">";
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
	}

	public void reloadGuild(IGuild guild) {
		defaultChannels.remove(guild);
		musicChannels.remove(guild);
	}

	private void registerEvents() {
		Reflections reflections = new Reflections("discordbot.event");
		Set<Class<? extends AbstractEventListener>> classes = reflections.getSubTypesOf(AbstractEventListener.class);
		for (Class<? extends AbstractEventListener> eventClass : classes) {
			try {
				AbstractEventListener eventListener = eventClass.getConstructor(DiscordBot.class).newInstance(this);
				if (eventListener.listenerIsActivated()) {
					client.getDispatcher().registerListener(eventListener);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	private void registerHandlers() {
		commands = new CommandHandler(this);
		gameHandler = new GameHandler(this);
		Template.setBot(this);
		out = new OutgoingContentHandler(this);
		timer = new Timer();
	}

	public String getUserName() {
		return client.getOurUser().getName();
	}

	public boolean setUserName(String newName) {
		if (isReady && !getUserName().equals(newName)) {
			try {
				client.changeUsername(newName);
				return true;
			} catch (DiscordException | RateLimitException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void addSongToQueue(String filename, IGuild guild) {
		File file = new File(Config.MUSIC_DIRECTORY + filename); // Get file
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		try {
			player.queue(file);
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	public void skipCurrentSong(IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).skipSong();
	}

	public void setVolume(IGuild guild, float vol) {
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		player.setVolume(vol);
	}


	public void handlePrivateMessage(IPrivateChannel channel, IUser author, IMessage message) {
		if (commands.isCommand(channel, message.getContent())) {
			commands.process(channel, author, message.getContent());
		} else {
			this.out.sendMessage(channel, this.chatBotHandler.chat(message.getContent()));
		}
	}

	public void handleMessage(IGuild guild, IChannel channel, IUser author, IMessage message) {
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
		} else if (commands.isCommand(channel, message.getContent())) {
			commands.process(channel, author, message.getContent());
		} else if (Config.BOT_CHATTING_ENABLED && settings.getOrDefault(SettingEnableChatBot.class).equals("true") &&
				!DefaultGuildSettings.getDefault(SettingBotChannel.class).equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class)) &&
				channel.getName().equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class))) {
			this.out.sendMessage(channel, this.chatBotHandler.chat(message.getContent()));
		}
	}

	public float getVolume(IGuild guild) {
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		return player.getVolume();
	}

	public void trackEnded(AudioPlayer.Track oldTrack, Optional<AudioPlayer.Track> nextTrack, IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).onTrackEnded(oldTrack, nextTrack);
	}

	public void trackStarted(AudioPlayer.Track track, IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).onTrackStarted(track);
	}

	public void stopMusic(IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).stopMusic();
	}

	public OMusic getCurrentlyPlayingSong(IGuild guild) {
		return MusicPlayerHandler.getAudioPlayerForGuild(guild, this).getCurrentlyPlaying();
	}

	public List<IUser> getCurrentlyListening(IGuild guild) {
		return MusicPlayerHandler.getAudioPlayerForGuild(guild, this).getUsersInVoiceChannel();
	}

	public boolean playRandomSong(IGuild guild) {
		return MusicPlayerHandler.getAudioPlayerForGuild(guild, this).playRandomSong();
	}
}