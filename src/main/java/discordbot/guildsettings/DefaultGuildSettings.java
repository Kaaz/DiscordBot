package discordbot.guildsettings;

import discordbot.core.ExitCode;
import discordbot.exceptions.DefaultSettingAlreadyExistsException;
import discordbot.main.Launcher;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultGuildSettings {
	private final static Map<String, AbstractGuildSetting> defaultSettings = new HashMap<>();
	private final static Map<Class<? extends AbstractGuildSetting>, String> classNameToKey = new HashMap<>();
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

	public static Map<String, AbstractGuildSetting> getDefaults() {
		return defaultSettings;
	}

	public static String getDefault(String key) {
		return defaultSettings.get(key).getDefault();
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
				AbstractGuildSetting settingObject = settingClass.getConstructor().newInstance();
				if (!defaultSettings.containsKey(settingObject.getKey())) {
					defaultSettings.put(settingObject.getKey(), settingObject);
					classNameToKey.put(settingClass, settingObject.getKey());
				} else {
					throw new DefaultSettingAlreadyExistsException(settingObject.getKey());
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		initialized = true;
	}
}