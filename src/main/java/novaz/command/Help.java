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
		setCmd("help");
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			AbstractCommand c = bot.commandHandler.getCommand(args[0]);
			if (c != null) {
				return ":information_source: :information_desk_person: " + Config.EOL +
						"Usage for " + c.getCmd() + Config.EOL +
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