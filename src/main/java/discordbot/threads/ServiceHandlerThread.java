package discordbot.threads;

import discordbot.core.AbstractService;
import discordbot.main.BotContainer;
import discordbot.main.Launcher;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceHandlerThread extends Thread {
	private BotContainer bot;
	private List<AbstractService> instances;

	public ServiceHandlerThread(BotContainer bot) {
		super("ServiceHandler");
		instances = new ArrayList<>();
		this.bot = bot;
	}

	private void initServices() {
		Reflections reflections = new Reflections("discordbot.service");
		Set<Class<? extends AbstractService>> classes = reflections.getSubTypesOf(AbstractService.class);
		for (Class<? extends AbstractService> serviceClass : classes) {
			try {
				instances.add(serviceClass.getConstructor(BotContainer.class).newInstance(bot));
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		boolean initialized = false;
		while (!Launcher.isBeingKilled) {
			try {
				if (bot.allShardsReady()) {
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
				Launcher.logToDiscord(e);
				e.printStackTrace();
			}
		}
	}
}
