package novaz.command.informative;

import novaz.command.CommandCategory;
import novaz.core.AbstractCommand;
import novaz.handler.CommandHandler;
import novaz.handler.GuildSettings;
import novaz.handler.TextHandler;
import novaz.handler.guildsettings.defaults.SettingCommandPrefix;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
		String CommandPrefix = GuildSettings.get(channel.getGuild()).getOrDefault(SettingCommandPrefix.class);
		if (args.length > 0) {
			AbstractCommand c = bot.commandHandler.getCommand(CommandHandler.filterPrefix(args[0], channel.getGuild()));
			if (c != null) {
				String ret = " :information_source: Help > " + c.getCommand() + " :information_source:" + Config.EOL;
				ret += ":keyboard: **command:** " + Config.EOL +
						Misc.makeTable(CommandPrefix + c.getCommand());
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
			String ret = "I know the following commands: " + Config.EOL + Config.EOL;
			HashMap<CommandCategory, ArrayList<String>> commandList = new HashMap<>();
			AbstractCommand[] commandObjects = bot.commandHandler.getCommandObjects();
			for (AbstractCommand command : commandObjects) {
				if (!commandList.containsKey(command.getCommandCategory())) {
					commandList.put(command.getCommandCategory(), new ArrayList<>());
				}
				commandList.get(command.getCommandCategory()).add(command.getCommand());
			}
			commandList.forEach((k, v) -> Collections.sort(v));
//			ret += styleTablePerCategory(commandList);
//			ret += styleIndentedTable(commandList);
			ret += styleOneTable(commandList);
			return ret + "for more details about a command use **" + CommandPrefix + "help <command>**" + Config.EOL;
		}
	}

	private String styleOneTable(HashMap<CommandCategory, ArrayList<String>> map) {
		ArrayList<String> list = new ArrayList<>();
		int columns = 4;
		int index = 0;
		for (CommandCategory category : CommandCategory.values()) {
			if (map.containsKey(category)) {
				while (index % columns != 0) {
					index++;
					list.add("");
				}
				list.add(" > " + category.getPackageName());
				for (int i = 1; i < columns; i++) {
					list.add("");
					index++;
				}
				index++;
				for (String cmd : map.get(category)) {
					list.add(cmd);
					index++;
				}
				for (int i = 0; i < columns; i++) {
					list.add("");
					index++;
				}
			}
		}
		return Misc.makeTable(list, 16, columns);
	}

	private String styleTablePerCategory(HashMap<CommandCategory, ArrayList<String>> map) {
		String table = "";
		for (CommandCategory category : CommandCategory.values()) {
			if (map.containsKey(category)) {
				table += category.getEmoticon() + " " + category.getPackageName() + Config.EOL;
				table += Misc.makeTable(map.get(category));
			}
		}
		return table;
	}

	private String styleIndentedTable(HashMap<CommandCategory, ArrayList<String>> map) {
		ArrayList<String> list = new ArrayList<>();
		int columns = 4;
		int index = 0;
		for (CommandCategory category : CommandCategory.values()) {
			if (map.containsKey(category)) {
				list.add(category.getPackageName());
				index++;
				for (int i = 0; i < columns; i++) {
					list.add("");
					index++;
				}
				for (String cmd : map.get(category)) {
					if (index % columns == 0) {
						list.add("");
						index++;
					}
					list.add(cmd);
					index++;
				}
				while (index % columns != 0) {
					index++;
					list.add("");
				}
			}
		}
		return Misc.makeTable(list, 16, columns);
	}
}