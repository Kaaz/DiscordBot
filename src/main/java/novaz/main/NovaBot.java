package novaz.main;

import novaz.core.AbstractEventListener;
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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class NovaBot {

	private IDiscordClient instance;
	private boolean isReady = false;
	private CommandHandler commandHandler;

	public void markReady(boolean ready) {
		this.isReady = ready;
		commandHandler.load();
	}

	public NovaBot() throws DiscordException {
		instance = new ClientBuilder().withToken("MjEyODM0MDYxMzA2MDM2MjI0.CoxpSw.9tqLPIvn1gUXaq1cT-_tdKYGT7s").login();
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

	private void registerHandlers() {
		commandHandler = new CommandHandler(this);
	}

	public String getUserName() {
		return instance.getOurUser().getName();
	}

	public boolean setUserName(String newName) {
		if (isReady) {
			try {
				instance.changeUsername(newName);
				return true;
			} catch (DiscordException | RateLimitException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void sendMessage(IChannel channel, String content) {
		try {
			new MessageBuilder(instance).withChannel(channel).withContent(content).build();
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			e.printStackTrace();
		}
	}

	public void handleMessage(IGuild guild, IChannel channel, IUser author, String content) {
		if (content.startsWith(CommandHandler.commandPrefix)) {
			commandHandler.process(guild, channel, author, content);
		}
	}
}
