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

	public CommandCategory getCommandCategory() {
		return commandCategory;
	}

	public void setCommandCategory(CommandCategory newCategory) {
		commandCategory = newCategory;
	}

	public abstract String execute(String[] args, IChannel channel, IUser author);
}
