package discordbot.command;

/**
 * Limits the usage of commands by adding a cooldown to commands
 */
public interface ICommandCooldown {

	/**
	 * gets the cooldown of a command
	 *
	 * @return cooldown in seconds
	 */
	long getCooldownDuration();

	/**
	 * cooldown on what scale?
	 *
	 * @return scope of the cooldown
	 */
	CooldownScope getScope();
}
