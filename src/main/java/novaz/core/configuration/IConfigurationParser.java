package novaz.core.configuration;

/**
 * Created on 30-8-2016
 */
public interface IConfigurationParser<Type> {

	Type parse(String value);
}
