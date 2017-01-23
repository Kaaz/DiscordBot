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
