package novaz.core;

import novaz.main.NovaBot;

/**
 * Gemaakt op 10-8-2016
 */
public abstract class AbstractCommand {

	protected String name = "My unimplemented command";
	protected String description = "Unset description";
	protected String cmd = "unimplemented";
	protected NovaBot bot;
	protected boolean requiresOp = true;

	protected void setOpOnly(boolean opOnly) {
		requiresOp = opOnly;
	}

	public boolean isOpOnly() {
		return requiresOp;
	}

	protected void setDescription(String d) {
		this.description = d;
	}

	public String getDescription() {
		return description;
	}

	public AbstractCommand(NovaBot bot) {
		this.bot = bot;
	}


	public String getCmd() {
		return cmd;
	}

	protected void setCmd(String c) {
		cmd = c;
	}

	public String getUsage() {
		return "Type !" + cmd;
	}

	public abstract String execute(String[] args, String sender, boolean isOp);
}
