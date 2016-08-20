package novaz.handler.guildsettings;

import novaz.exceptions.DefaultSettingAlreadyExistsException;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGuildSettings {
	private final static Map<String, AbstractGuildSetting> defaultSettings = new ConcurrentHashMap<>();
	private final static Map<Class<? extends AbstractGuildSetting>, String> classNameToKey = new ConcurrentHashMap<>();
	private static boolean initialized = false;

	static {
		try {
			initSettings();
		} catch (DefaultSettingAlreadyExistsException e) {
			e.printStackTrace();
			System.out.println(e.toString());
			System.exit(-1);
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

	public static String getDefault(Class<? extends AbstractGuildSetting> clazz) {
		return defaultSettings.get(getKey(clazz)).getDefault();
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
		Reflections reflections = new Reflections("novaz.handler.guildsettings");
		Set<Class<? extends AbstractGuildSetting>> classes = reflections.getSubTypesOf(AbstractGuildSetting.class);
		for (Class<? extends AbstractGuildSetting> clazz : classes) {
			try {
				AbstractGuildSetting obj = clazz.getConstructor().newInstance();
				if (!defaultSettings.containsKey(obj.getKey())) {
					defaultSettings.put(obj.getKey(), obj);
					classNameToKey.put(clazz, obj.getKey());
				} else {
					throw new DefaultSettingAlreadyExistsException(obj.getKey());
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		initialized = true;
	}
}