package novaz.main;

import novaz.core.AbstractEventListener;
import novaz.db.model.OMusic;
import novaz.guildsettings.DefaultGuildSettings;
import novaz.guildsettings.defaults.SettingActiveChannels;
import novaz.guildsettings.defaults.SettingBotChannel;
import novaz.guildsettings.defaults.SettingEnableChatBot;
import novaz.handler.*;
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

public class NovaBot {

	public final long startupTimeStamp;
	public IDiscordClient instance;
	public CommandHandler commands;
	public Timer timer = new Timer();
	public String mentionMe;
	public ChatBotHandler chatBotHandler = null;
	private GameHandler gameHandler = null;
	public OutgoingContentHandler out = null;
	private boolean isReady = false;
	public boolean statusLocked = false;
	private Map<IGuild, IChannel> defaultChannels = new ConcurrentHashMap<>();
	public static final Logger LOGGER = LoggerFactory.getLogger(NovaBot.class);

	public NovaBot() throws DiscordException {
		registerHandlers();
		instance = new ClientBuilder().withToken(Config.BOT_TOKEN).login();
		registerEvents();
		startupTimeStamp = System.currentTimeMillis() / 1000L;
	}

	public boolean isReady() {
		return isReady;
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
	 * Bot will start working once its marked ready
	 *
	 * @param ready ready to get started
	 */
	public void markReady(boolean ready) {
		setUserName(Config.BOT_NAME);
		loadConfiguration();
		mentionMe = "<@" + this.instance.getOurUser().getID() + ">";
		timer = new Timer();
		TextHandler.setBot(this);
		gameHandler = new GameHandler(this);
		out = new OutgoingContentHandler(this);
		this.isReady = ready;
	}

	public void loadConfiguration() {
		commands.load();
		TextHandler.getInstance().load();
		defaultChannels = new ConcurrentHashMap<>();
		chatBotHandler = new ChatBotHandler();
	}

	private void registerEvents() {
		Reflections reflections = new Reflections("novaz.event");
		Set<Class<? extends AbstractEventListener>> classes = reflections.getSubTypesOf(AbstractEventListener.class);
		for (Class<? extends AbstractEventListener> c : classes) {
			try {
				AbstractEventListener eventListener = c.getConstructor(NovaBot.class).newInstance(this);
				if (eventListener.listenerIsActivated()) {
					instance.getDispatcher().registerListener(eventListener);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	private void registerHandlers() {
		commands = new CommandHandler(this);
	}

	public String getUserName() {
		return instance.getOurUser().getName();
	}

	public boolean setUserName(String newName) {
		if (isReady && !getUserName().equals(newName)) {
			try {
				instance.changeUsername(newName);
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