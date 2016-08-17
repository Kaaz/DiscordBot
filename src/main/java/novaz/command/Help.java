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
	public String[] getUsage() {
		return new String[]{
				"help //index of all commands",
				"help <command> //usage for that command"};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			AbstractCommand c = bot.commandHandler.getCommand(args[0]);
			if (c != null) {
				String ret = ":information_source: :information_desk_person: ";
				ret += ":information_source: Help for " + Config.BOT_COMMAND_PREFIX + c.getCommand() + Config.EOL;
				ret += c.getDescription() + Config.EOL;
				if (c.getUsage().length > 0) {
					ret += "Usage:```php";
					for (String line : c.getUsage()) {
						ret += line + Config.EOL;
					}
					ret += "```";
				}
				return ret;

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