package novaz.main;

import novaz.core.AbstractEventListener;
import novaz.db.model.OServer;
import novaz.db.table.TServers;
import novaz.handler.CommandHandler;
import novaz.handler.MusicPlayerHandler;
import novaz.handler.TextHandler;
import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;

public class NovaBot {

	public IDiscordClient instance;
	private boolean isReady = false;
	public CommandHandler commandHandler;
	private float volume;
	public final Timer timer = new Timer();

	public NovaBot() throws DiscordException {
		registerHandlers();
		instance = new ClientBuilder().withToken(Config.BOT_TOKEN).login();
		registerEvents();
	}

	public void markReady(boolean ready) {
		this.isReady = ready;
		setUserName(Config.BOT_NAME);
		loadConfiguration();
	}

	public void loadConfiguration() {
		commandHandler.load();
		TextHandler.getInstance().load();
	}

	private void registerEvents() {
		Reflections reflections = new Reflections("novaz.event");
		Set<Class<? extends AbstractEventListener>> classes = reflections.getSubTypesOf(AbstractEventListener.class);
		for (Class<? extends AbstractEventListener> c : classes) {
			try {
				AbstractEventListener eventListener = c.getConstructor(NovaBot.class).newInstance(this);
				instance.getDispatcher().registerListener(eventListener);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public void addCustomCommand(IGuild server, String command, String output) {
		OServer serv = TServers.findBy(server.getID());
		commandHandler.addCustomCommand(serv.id, command, output);
	}

	public void removeCustomCommand(IGuild server, String command) {
		OServer serv = TServers.findBy(server.getID());
		commandHandler.removeCustomCommand(serv.id, command);
	}

	private void registerHandlers() {
		commandHandler = new CommandHandler(this);
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
		System.out.println("Adding: " + Config.MUSIC_DIRECTORY + filename);
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

	public IMessage sendMessage(IChannel channel, String content) {
		try {
			return new MessageBuilder(instance).withChannel(channel).withContent(content).build();
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void handleMessage(IGuild guild, IChannel channel, IUser author, IMessage content) {
		if (content.getContent().startsWith(Config.BOT_COMMAND_PREFIX)) {
			commandHandler.process(guild, channel, author, content);
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
}
