package discordbot.games;

import discordbot.main.Config;

/**
 * a turn in a game
 */
public abstract class GameTurn {
	private String commandPrefix = Config.BOT_COMMAND_PREFIX;

	abstract public boolean parseInput(String input);

	abstract public String getInputErrorMessage();

	public String getCommandPrefix() {
		return commandPrefix;
	}

	public void setCommandPrefix(String commandPrefix) {
		this.commandPrefix = commandPrefix;
	}
}
