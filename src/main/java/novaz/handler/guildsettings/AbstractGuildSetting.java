package novaz.handler.guildsettings;

abstract public class AbstractGuildSetting {

	/**
	 * key for the configuration
	 *
	 * @return keyname
	 */
	public abstract String getKey();

	/**
	 * default value for the config
	 *
	 * @return default
	 */
	public abstract String getDefault();

	/**
	 * Description for the config
	 *
	 * @return short description
	 */
	public abstract String getDescription();
}
