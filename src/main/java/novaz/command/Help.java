package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !help
 * help function
 */
public class Help extends AbstractCommand {
	public Help(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "An attempt to help out";
	}

	@Override
	public String getCommand() {
		return "help";
	}

	@Override
	public String getUsage() {
		return "help or help <command>";
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			AbstractCommand c = bot.commandHandler.getCommand(args[0]);
			if (c != null) {
				return ":information_source: :information_desk_person: " + Config.EOL +
						"Usage for " + c.getCommand() + Config.EOL +
						c.getUsage() + Config.EOL +
						c.getDescription();
			}
			return TextHandler.get("command_help_donno");
		} else {
			String ret = "Commands:";
			for (String command : bot.commandHandler.getCommands()) {
				ret += " " + command;
			}
			return ret;
		}
	}
}