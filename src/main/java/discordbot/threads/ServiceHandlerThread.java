package discordbot.threads;

import discordbot.core.AbstractService;
import discordbot.main.Launcher;
import discordbot.main.NovaBot;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceHandlerThread extends Thread {
	private NovaBot bot;
	private List<Class<? extends AbstractService>> services;

	public ServiceHandlerThread(NovaBot bot) {
		super("ServiceHandler");
		services = new ArrayList<>();
		this.bot = bot;
		collectServices();
	}

	private void collectServices() {
		Reflections reflections = new Reflections("discordbot.service");
		Set<Class<? extends AbstractService>> classes = reflections.getSubTypesOf(AbstractService.class);
		for (Class<? extends AbstractService> s : classes) {
			services.add(s);
		}
	}

	@Override
	public void run() {
		while (!Launcher.killAllThreads) {
			try {
				if (bot.isReady()) {
					try {
						if (bot != null) {
							for (Class<? extends AbstractService> serviceClass : services) {
								AbstractService serviceInstance = serviceClass.getConstructor(NovaBot.class).newInstance(bot);
								serviceInstance.start();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				sleep(10_000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
