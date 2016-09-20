package discordbot.guildsettings;

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
	public abstract String[] getDescription();

	/**
	 * Checks if the value is a valid setting
	 *
	 * @param input value to check
	 * @return wheneter it is a valid value
	 */
	public abstract boolean isValidValue(String input);
}
