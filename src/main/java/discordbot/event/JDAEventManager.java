package discordbot.event;

import discordbot.main.BotContainer;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JDAEventManager implements IEventManager {
	private final BotContainer container;
	private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
	private final ExecutorService executor;

	public JDAEventManager(BotContainer container) {
		this.container = container;
		executor = Executors.newCachedThreadPool();
	}

	@Override
	public void register(Object listener) {
		if (!(listener instanceof EventListener)) {
			throw new IllegalArgumentException("Listener must implement EventListener");
		}
		listeners.add((EventListener) listener);
	}

	@Override
	public void unregister(Object listener) {
		if (listener instanceof EventListener) {
			listeners.remove(listener);
		}
	}

	@Override
	public List<Object> getRegisteredListeners() {
		return Collections.unmodifiableList(new LinkedList<>(listeners));
	}

	@Override
	public void handle(Event event) {
		if (executor.isShutdown()) {
			container.reportError(new Exception("NO EVENT MANAGER"), "JDAEventManager", "is kill");
			return;
		}
		final List<Object> cachedListeners = getRegisteredListeners();
		executor.submit(() -> {
			for (Object listener : cachedListeners) {
				try {
					((EventListener) listener).onEvent(event);
				} catch (Exception e) {
					container.reportError(e, "JDAEvent", event.getClass().getName());
					e.printStackTrace();
				}
			}
		});
	}
}