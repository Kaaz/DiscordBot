package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
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
				String ret = " Help :information_source: " + Config.EOL;
				ret += "**command:** " + Config.EOL +
						Config.BOT_COMMAND_PREFIX + " " + c.getCommand() + Config.EOL;
				ret += "**Description:** " + Config.EOL +
						" " + c.getDescription() + Config.EOL;
				if (c.getUsage().length > 0) {
					ret += "**Usages**:```php" + Config.EOL;
					for (String line : c.getUsage()) {
						ret += line + Config.EOL;
					}
					ret += "```";
				}
				return ret;
			}
			return TextHandler.get("command_help_donno");
		} else {
			String ret = ":information_source: All available commands:" + Config.EOL +
					"```ini" + Config.EOL;
			int counter = 0;
			ArrayList<String> sortedList = new ArrayList<>();
			Collections.addAll(sortedList, bot.commandHandler.getCommands());
			Collections.sort(sortedList);
			for (String command : sortedList) {
				counter++;
				ret += String.format("%-16s", command);
				if (counter % 5 == 0) {
					ret += Config.EOL;
				}
			}
			return ret + Config.EOL +
					"```" + Config.EOL +
					"for more details about a command use **" + Config.BOT_COMMAND_PREFIX + "help <command>**" + Config.EOL;
		}
	}
}