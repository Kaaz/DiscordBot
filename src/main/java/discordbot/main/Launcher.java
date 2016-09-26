package discordbot.main;

import com.wezinkhof.configuration.ConfigurationBuilder;
import discordbot.core.DbUpdate;
import discordbot.core.ExitCode;
import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.threads.ServiceHandlerThread;
import discordbot.util.YTUtil;
import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;
import sx.blah.discord.handle.obj.Presences;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Properties;

public class Launcher {
	public static boolean killAllThreads = false;
	private static ProgramVersion version = new ProgramVersion(1);

	public static ProgramVersion getVersion() {
		return version;
	}

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		WebDb.init();
		Properties props = new Properties();
		props.load(Launcher.class.getClassLoader().getResourceAsStream("version.properties"));
		Launcher.version = ProgramVersion.fromString(String.valueOf(props.getOrDefault("version", "1")));
		addEnum(Presences.class, "DND");
		DiscordBot.LOGGER.info("Started with version: " + Launcher.version);
		DbUpdate dbUpdate = new DbUpdate(WebDb.get());
		dbUpdate.updateToCurrent();
		if (Config.BOT_ENABLED) {
			DiscordBot nb = null;
			try {
				nb = new DiscordBot();
				Thread serviceHandler = new ServiceHandlerThread(nb);
				serviceHandler.setDaemon(true);
				serviceHandler.start();
			} catch (DiscordException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				Launcher.stop(ExitCode.SHITTY_CONFIG);
			}
		} else {
			Logger.fatal("Bot not enabled, enable it in the config. You can do this by setting bot_enabled=true");
			Launcher.stop(ExitCode.SHITTY_CONFIG);
		}
	}

	/**
	 * Stop the bot!
	 *
	 * @param reason why!?
	 */
	public static void stop(ExitCode reason) {

		DiscordBot.LOGGER.error("Exiting", reason);
		System.exit(reason.getCode());
	}

	/**
	 * helper function, retrieves youtubeTitle for mp3 files which contain youtube videocode as filename
	 */
	public static void fixExistingYoutubeFiles() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		for (String file : fileList) {
			System.out.println(file);
			String videocode = file.replace(".mp3", "");
			OMusic rec = TMusic.findByYoutubeId(videocode);
			rec.youtubeTitle = YTUtil.getTitleFromPage(videocode);
			rec.youtubecode = videocode;
			rec.filename = videocode + ".mp3";
			TMusic.update(rec);
		}
	}
	private static ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

	private static void setFailsafeFieldValue(Field field, Object target, Object value) throws NoSuchFieldException,
			IllegalAccessException {

		// let's make the field accessible
		field.setAccessible(true);

		// next we change the modifier in the Field instance to
		// not be final anymore, thus tricking reflection into
		// letting us modify the static final field
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		int modifiers = modifiersField.getInt(field);

		// blank out the final bit in the modifiers int
		modifiers &= ~Modifier.FINAL;
		modifiersField.setInt(field, modifiers);

		FieldAccessor fa = reflectionFactory.newFieldAccessor(field, false);
		fa.set(target, value);
	}

	private static void blankField(Class<?> enumClass, String fieldName) throws NoSuchFieldException,
			IllegalAccessException {
		for (Field field : Class.class.getDeclaredFields()) {
			if (field.getName().contains(fieldName)) {
				AccessibleObject.setAccessible(new Field[] { field }, true);
				setFailsafeFieldValue(field, enumClass, null);
				break;
			}
		}
	}

	private static void cleanEnumCache(Class<?> enumClass) throws NoSuchFieldException, IllegalAccessException {
		blankField(enumClass, "enumConstantDirectory"); // Sun (Oracle?!?) JDK 1.5/6
		blankField(enumClass, "enumConstants"); // IBM JDK
	}

	private static ConstructorAccessor getConstructorAccessor(Class<?> enumClass, Class<?>[] additionalParameterTypes)
			throws NoSuchMethodException {
		Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
		parameterTypes[0] = String.class;
		parameterTypes[1] = int.class;
		System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
		return reflectionFactory.newConstructorAccessor(enumClass.getDeclaredConstructor(parameterTypes));
	}

	private static Object makeEnum(Class<?> enumClass, String value, int ordinal, Class<?>[] additionalTypes,
								   Object[] additionalValues) throws Exception {
		Object[] parms = new Object[additionalValues.length + 2];
		parms[0] = value;
		parms[1] = Integer.valueOf(ordinal);
		System.arraycopy(additionalValues, 0, parms, 2, additionalValues.length);
		return enumClass.cast(getConstructorAccessor(enumClass, additionalTypes).newInstance(parms));
	}

	/**
	 * Add an enum instance to the enum class given as argument
	 *
	 * @param <T> the type of the enum (implicit)
	 * @param enumType the class of the enum to be modified
	 * @param enumName the name of the new enum instance to be added to the class.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Enum<?>> void addEnum(Class<T> enumType, String enumName) {

		// 0. Sanity checks
		if (!Enum.class.isAssignableFrom(enumType)) {
			throw new RuntimeException("class " + enumType + " is not an instance of Enum");
		}

		// 1. Lookup "$VALUES" holder in enum class and get previous enum instances
		Field valuesField = null;
		Field[] fields = Presences.class.getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().contains("$VALUES")) {
				valuesField = field;
				break;
			}
		}
		AccessibleObject.setAccessible(new Field[] { valuesField }, true);

		try {

			// 2. Copy it
			T[] previousValues = (T[]) valuesField.get(enumType);
			List<T> values = new ArrayList<T>(Arrays.asList(previousValues));

			// 3. build new enum
			T newValue = (T) makeEnum(enumType, // The target enum class
					enumName, // THE NEW ENUM INSTANCE TO BE DYNAMICALLY ADDED
					values.size(),
					new Class<?>[] {}, // could be used to pass values to the enum constuctor if needed
					new Object[] {}); // could be used to pass values to the enum constuctor if needed

			// 4. add new value
			values.add(newValue);

			// 5. Set new values field
			setFailsafeFieldValue(valuesField, null, values.toArray((T[]) Array.newInstance(enumType, 0)));

			// 6. Clean enum cache
			cleanEnumCache(enumType);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}