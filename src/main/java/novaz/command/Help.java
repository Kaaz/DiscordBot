package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Collections;

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
				String ret = " :information_source: Help > " + c.getCommand() + " :information_source:" + Config.EOL;
				ret += ":keyboard: **command:** " + Config.EOL +
						Misc.makeTable(Config.BOT_COMMAND_PREFIX + c.getCommand());
				ret += ":notepad_spiral: **Description:** " + Config.EOL +
						Misc.makeTable(c.getDescription());
				if (c.getUsage().length > 0) {
					ret += ":gear: **Usages**:```php" + Config.EOL;
					for (String line : c.getUsage()) {
						ret += line + Config.EOL;
					}
					ret += "```";
				}
				return ret;
			}
			return TextHandler.get("command_help_donno");
		} else {
			String ret = ":information_source: All available commands:" + Config.EOL;
			ArrayList<String> sortedList = new ArrayList<>();
			Collections.addAll(sortedList, bot.commandHandler.getCommands());
			Collections.sort(sortedList);
			ret += Misc.makeTable(sortedList);
			return ret + "for more details about a command use **" + Config.BOT_COMMAND_PREFIX + "help <command>**" + Config.EOL;
		}
	}
}