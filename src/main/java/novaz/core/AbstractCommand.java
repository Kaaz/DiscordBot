package novaz.core;

import novaz.command.CommandCategory;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public abstract class AbstractCommand {

	protected NovaBot bot;
	protected CommandCategory commandCategory = CommandCategory.UNKNOWN;

	public AbstractCommand(NovaBot bot) {
		this.bot = bot;
	}

	/**
	 * A short discription of the method
	 *
	 * @return description
	 */
	public abstract String getDescription();

	/**
	 * What should be typed to trigger this command (Without prefix)
	 *
	 * @return command
	 */
	public abstract String getCommand();

	/**
	 * How to use the command?
	 *
	 * @return command usage
	 */
	public abstract String[] getUsage();

	/**
	 * aliases to call the command
	 *
	 * @return array of aliases
	 */
	public abstract String[] getAliases();

	public CommandCategory getCommandCategory() {
		return commandCategory;
	}

	/**
	 * The command will be set to the category matching the last part of the package name.
	 *
	 * @param newCategory category of the command
	 */
	public void setCommandCategory(CommandCategory newCategory) {
		commandCategory = newCategory;
	}

	public abstract String execute(String[] args, IChannel channel, IUser author);
}
