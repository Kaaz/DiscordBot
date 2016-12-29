package discordbot.guildsettings;

import net.dv8tion.jda.core.entities.Guild;

public interface IGuildSettingType {

	/**
	 * Display name of the setting type
	 *
	 * @return name
	 */
	String typeName();

	/**
	 * Checks if a setting is valid
	 *
	 * @param guild the guild to check in
	 * @param value the new value
	 * @return does the value validate?
	 */
	boolean validate(Guild guild, String value);

	/**
	 * Converts the input to a value which can be used to store in the db
	 *
	 * @param guild the guild to check in
	 * @param value the input value
	 * @return a db-save value
	 */
	String fromInput(Guild guild, String value);

	/**
	 * Converts the db-save value back to a fancy db
	 *
	 * @param guild the guild to check in
	 * @param value the db-value to the fancy display
	 * @return a nicely formated value
	 */
	String toDisplay(Guild guild, String value);
}
