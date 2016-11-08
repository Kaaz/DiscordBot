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
	 * Whether a config setting is read-only
	 * Used to save guild-specific settings which are set automatically
	 *
	 * @return is readonly?
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * Checks if the value is a valid setting
	 *
	 * @param input value to check
	 * @return wheneter it is a valid value
	 */
	public abstract boolean isValidValue(String input);
}
