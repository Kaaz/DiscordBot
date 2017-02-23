/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.event;

import discordbot.main.BotContainer;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JDAEventManager implements IEventManager {
    private final BotContainer container;
    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
//    private final ExecutorService executor;

    public JDAEventManager(BotContainer container) {
        this.container = container;
//        executor = Executors.newCachedThreadPool();
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
//        if (executor.isShutdown()) {
//            container.reportError(new Exception("NO EVENT MANAGER"), "JDAEventManager", "is kill");
//            return;
//        }
        final List<Object> cachedListeners = getRegisteredListeners();
        for (Object listener : cachedListeners) {
            try {
                ((EventListener) listener).onEvent(event);
            } catch (Exception e) {
                container.reportError(e, "JDAEvent", event.getClass().getName());
                e.printStackTrace();
            }
        }
    }
}