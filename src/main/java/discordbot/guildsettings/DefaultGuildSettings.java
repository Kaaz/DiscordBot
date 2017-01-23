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

package discordbot.guildsettings;

import discordbot.core.ExitCode;
import discordbot.exceptions.DefaultSettingAlreadyExistsException;
import discordbot.main.Launcher;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DefaultGuildSettings {
	private final static Map<String, AbstractGuildSetting> defaultSettings = new HashMap<>();
	private final static Map<Class<? extends AbstractGuildSetting>, String> classNameToKey = new HashMap<>();
	private static final TreeSet<String> tags = new TreeSet<>();
	private static boolean initialized = false;

	static {
		try {
			initSettings();
		} catch (DefaultSettingAlreadyExistsException e) {
			e.printStackTrace();
			System.out.println(e.toString());
			Launcher.stop(ExitCode.GENERIC_ERROR);
		}
	}

	public static TreeSet<String> getAllTags() {
		return new TreeSet<>(tags);
	}

	public static Map<String, AbstractGuildSetting> getDefaults() {
		return defaultSettings;
	}

	public static String getDefault(String key) {
		return defaultSettings.get(key).getDefault();
	}

	public static int countSettings() {
		return countSettings(true);
	}

	public static int countSettings(boolean includeReadOnly) {
		if (includeReadOnly) {
			return defaultSettings.keySet().size();
		}
		return (int) defaultSettings.values().stream().filter(abstractGuildSetting -> !abstractGuildSetting.isReadOnly()).count();
	}

	public static List<String> getWritableKeys() {
		ArrayList<String> set = new ArrayList<>();
		for (Map.Entry<String, AbstractGuildSetting> entry : defaultSettings.entrySet()) {
			if (entry.getValue().isReadOnly()) {
				continue;
			}
			set.add(entry.getKey());
		}
		return set;
	}

	public static AbstractGuildSetting get(String key) {
		return defaultSettings.get(key);
	}

	public static String getDefault(Class<? extends AbstractGuildSetting> guildSettingClass) {
		return defaultSettings.get(getKey(guildSettingClass)).getDefault();
	}

	public static boolean isValidKey(String key) {
		return defaultSettings.containsKey(key);
	}

	public static String getKey(Class<? extends AbstractGuildSetting> clazz) {
		return classNameToKey.get(clazz);
	}

	private static void initSettings() throws DefaultSettingAlreadyExistsException {
		if (initialized) {
			return;
		}
		Reflections reflections = new Reflections("discordbot.guildsettings");
		Set<Class<? extends AbstractGuildSetting>> classes = reflections.getSubTypesOf(AbstractGuildSetting.class);
		for (Class<? extends AbstractGuildSetting> settingClass : classes) {
			try {
				AbstractGuildSetting setting = settingClass.getConstructor().newInstance();
				if (!defaultSettings.containsKey(setting.getKey())) {
					defaultSettings.put(setting.getKey(), setting);
					classNameToKey.put(settingClass, setting.getKey());
					for (String tag : setting.getTags()) {
						if (!tags.contains(tag)) {
							tags.add(tag);
						}
					}
				} else {
					throw new DefaultSettingAlreadyExistsException(setting.getKey());
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		initialized = true;
	}
}