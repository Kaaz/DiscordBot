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

package emily.event;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import emily.core.Logger;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JDAEventManager implements IEventManager {

    private List<Object> listeners = new LinkedList<>();
    private final DiscordBot bot;
    private final ThreadPoolExecutor threadExecutor;

    public JDAEventManager(DiscordBot bot) {
        ThreadFactoryBuilder threadBuilder = new ThreadFactoryBuilder();
        threadBuilder.setNameFormat(String.format("shard-%02d-command-%%d", bot.getShardId()));
        this.threadExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadBuilder.build());
        this.bot = bot;
    }

    @Override
    public void register(Object listener) {
        if (!(listener instanceof EventListener)) {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.add(listener);
    }

    @Override
    public void unregister(Object listener) {
        listeners.remove(listener);
    }

    @Override
    public void handle(Event event) {
        threadExecutor.submit(() -> {
            bot.getContainer().setLastAction(event.getJDA().getShardInfo() == null ? 0 : event.getJDA().getShardInfo().getShardId(), System.currentTimeMillis());
            bot.updateJda(event.getJDA());
            if (!(event.getJDA().getStatus() == JDA.Status.CONNECTED)) {
                return;
            }
            List<Object> listenerCopy = new LinkedList<>(listeners);
            for (Object listener : listenerCopy) {
                try {
                    ((EventListener) listener).onEvent(event);
                } catch (PermissionException throwable) {
                    Logger.fatal("unchecked permission error!");
                    Logger.fatal(throwable);
                } catch (Throwable throwable) {
                    Logger.fatal(throwable);
                    bot.getContainer().reportError(throwable);
                }
            }
        });
    }

    @Override
    public List<Object> getRegisteredListeners() {
        return this.listeners;
    }

}
