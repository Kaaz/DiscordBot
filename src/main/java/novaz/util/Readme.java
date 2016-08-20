package novaz.util;

import novaz.core.AbstractCommand;
import novaz.core.ConfigurationBuilder;
import novaz.handler.CommandHandler;
import novaz.main.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Collection of methods to help me out in maintaining the readme file
 */
public class Readme {

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		readmeCommandDetailsList();
	}

	/**
	 * makes a sorted list of all commands with description
	 */
	private static String readmeCommandSimpleList() {
		String s = "";
		CommandHandler commandHandler = new CommandHandler();
		commandHandler.load();
		ArrayList<String> sortedCommandList = new ArrayList<>();
		Collections.addAll(sortedCommandList, commandHandler.getCommands());
		Collections.sort(sortedCommandList);
		for (String commandName : sortedCommandList) {
			AbstractCommand command = commandHandler.getCommand(commandName);
			s += "* [" + command.getCommand() + "](#" + command.getCommand() + ")" + Config.EOL;
		}
		return s;
	}

	private static String readmeCommandDetailsList() {
		String text = "";
		CommandHandler commandHandler = new CommandHandler();
		commandHandler.load();
		ArrayList<String> sortedCommandList = new ArrayList<>();
		Collections.addAll(sortedCommandList, commandHandler.getCommands());
		Collections.sort(sortedCommandList);
		for (String commandName : sortedCommandList) {
			AbstractCommand command = commandHandler.getCommand(commandName);
			text += "### " + command.getCommand() + Config.EOL + Config.EOL;
			text += command.getDescription() + Config.EOL + Config.EOL;
			if (command.getUsage().length > 0) {
				text += Config.EOL;
				text += "#### Usage" + Config.EOL + Config.EOL;
				text += "```php" + Config.EOL;
				for (String line : command.getUsage()) {
					text += line + Config.EOL;
				}
				text += ("```") + Config.EOL;
			}
		}
		return text;
	}

}
