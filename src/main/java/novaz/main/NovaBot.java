package novaz.main;

import novaz.core.AbstractEventListener;
import novaz.db.model.RServer;
import novaz.db.table.TServers;
import novaz.handler.CommandHandler;
import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
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
import java.util.Set;

public class NovaBot {

	private IDiscordClient instance;
	private boolean isReady = false;
	private CommandHandler commandHandler;

	public void markReady(boolean ready) {
		this.isReady = ready;
		commandHandler.load();
		setUserName(Config.BOT_NAME);
	}

	public NovaBot() throws DiscordException {
		instance = new ClientBuilder().withToken(Config.BOT_TOKEN).login();
		registerEvents();
		registerHandlers();
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
		RServer serv = TServers.findBy(server.getID());
		commandHandler.addCustomCommand(serv.id, command, output);
	}

	public void removeCustomCommand(IGuild server, String command) {
		RServer serv = TServers.findBy(server.getID());
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

	public void playAudioFromFile(String filename, IGuild guild) {
		System.out.println(Config.MUSIC_DIRECTORY + filename);
		File file = new File(Config.MUSIC_DIRECTORY + filename); // Get file
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild); // Get AudioPlayer for guild
		try {
			player.queue(file);
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	public void setVolume(IGuild guild, float vol) {
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		player.setVolume(vol);
	}

	public void sendMessage(IChannel channel, String content) {
		try {
			new MessageBuilder(instance).withChannel(channel).withContent(content).build();
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			e.printStackTrace();
		}
	}

	public void handleMessage(IGuild guild, IChannel channel, IUser author, String content) {
		if (content.startsWith(Config.BOT_COMMAND_PREFIX)) {
			commandHandler.process(guild, channel, author, content);
		}
	}
}
