package novaz.core.configuration.types;

import novaz.core.configuration.IConfigurationParser;

/**
 * Created on 30-8-2016
 */
public class ConfigurationString implements IConfigurationParser<String> {
	@Override
	public String parse(String value) {
		return value;
	}
}
