package novaz.core;

import novaz.core.annotation.Option;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;

public class ConfigurationBuilder {
	private final File configFile;
	private final Properties properties;
	private final Class configclass;

	public ConfigurationBuilder(Class configclass, File configFile) {
		this.configFile = configFile;
		this.configclass = configclass;
		this.properties = new Properties();
	}


	public void build() throws IOException {
		if (configFile == null) throw new IllegalStateException("File not initialized");
		Reflections reflections = new Reflections(new org.reflections.util.ConfigurationBuilder()
				.setUrls(ClasspathHelper.forClass(configclass))
				.addScanners(new FieldAnnotationsScanner()));
		Set<Field> options = reflections.getFieldsAnnotatedWith(Option.class);
		if (configFile.exists()) properties.load(new FileInputStream(configFile));

		options.forEach(o -> {
			Option option = o.getAnnotation(Option.class);
			try {
				String variableName = o.getName().toLowerCase();
				Object defaultValue = o.get(null);
				Object value = configFile.exists() ? properties.getOrDefault(variableName, defaultValue) : defaultValue;
				if (value.getClass().isAssignableFrom(String.class)) {
					o.setAccessible(true);
					o.set(null, value);
					properties.setProperty(variableName, (String) value);
				}
			} catch (IllegalAccessException e) {
				Logger.fatal("Could not load configuration, IllegalAccessException");
			}
		});
		properties.store(new FileOutputStream(configFile), null);
	}
}
