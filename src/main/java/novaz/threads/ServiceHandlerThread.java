package novaz.threads;

import novaz.core.AbstractService;
import novaz.main.Launcher;
import novaz.main.NovaBot;
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
		Reflections reflections = new Reflections("novaz.service");
		Set<Class<? extends AbstractService>> classes = reflections.getSubTypesOf(AbstractService.class);
		for (Class<? extends AbstractService> s : classes) {
			services.add(s);
		}
	}

	@Override
	public void run() {
		long lastTime = System.nanoTime();
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
				lastTime = System.nanoTime();
				sleep(10_000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
