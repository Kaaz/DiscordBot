package discordbot.threads;

import discordbot.core.AbstractService;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceHandlerThread extends Thread {
	private DiscordBot bot;
	private List<AbstractService> instances;

	public ServiceHandlerThread(DiscordBot bot) {
		super("ServiceHandler");
		instances = new ArrayList<>();
		this.bot = bot;
	}

	private void initServices() {
		Reflections reflections = new Reflections("discordbot.service");
		Set<Class<? extends AbstractService>> classes = reflections.getSubTypesOf(AbstractService.class);
		for (Class<? extends AbstractService> serviceClass : classes) {
			try {
				instances.add(serviceClass.getConstructor(DiscordBot.class).newInstance(bot));
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		boolean initialized = false;
		while (!Launcher.killAllThreads) {
			try {
				if (bot.isReady()) {
					if (bot != null) {
						if (!initialized) {
							initServices();
						}
						initialized = true;
					}
					for (AbstractService instance : instances) {
						instance.start();
					}
				}
				sleep(10_000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
