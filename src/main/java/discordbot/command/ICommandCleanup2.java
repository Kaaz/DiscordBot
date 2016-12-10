package discordbot.command;

/**
 * Indicating that a command has data/cache to clean up after a while
 */
public interface ICommandCleanup2 {
	/**
	 * This method is called in the cleanup service {@see discordbot.service.BotCleanupService}
	 * to clean up cached data
	 */
	public void cleanup();
}
