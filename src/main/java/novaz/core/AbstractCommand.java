package novaz.core;

import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * Gemaakt op 10-8-2016
 */
public abstract class AbstractCommand {

	protected String name = "My unimplemented command";
	protected String description = "Unset description";
	protected String cmd = "unimplemented";
	protected NovaBot bot;

	public AbstractCommand(NovaBot bot) {
		this.bot = bot;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription(String d) {
		this.description = d;
	}

	public String getCmd() {
		return cmd;
	}

	protected void setCmd(String c) {
		cmd = c;
	}

	public String getUsage() {
		return "Type "+ Config.BOT_COMMAND_PREFIX + cmd;
	}

	public abstract String execute(String[] args, IChannel channel, IUser author);
}
