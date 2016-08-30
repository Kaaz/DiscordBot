package novaz.core.configuration.types;

import novaz.core.configuration.IConfigurationParser;

/**
 * Created on 30-8-2016
 */
public class ConfigurationBoolean implements IConfigurationParser<Boolean> {
	@Override
	public Boolean parse(String value) {
		return Boolean.parseBoolean(value);
	}
}
