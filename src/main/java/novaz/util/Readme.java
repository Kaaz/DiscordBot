package novaz.util;

import novaz.core.AbstractCommand;
import novaz.core.ConfigurationBuilder;
import novaz.handler.CommandHandler;
import novaz.main.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Collection of methos to help me out in maintaining the readme file
 */
public class Readme {

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		readmeCommandDetailsList();
	}

	/**
	 * makes a sorted list of all commands with description
	 */
	private static void readmeCommandSimpleList() {
		CommandHandler commandHandler = new CommandHandler();
		commandHandler.load();
		ArrayList<String> sortedCommandList = new ArrayList<>();
		Collections.addAll(sortedCommandList, commandHandler.getCommands());
		Collections.sort(sortedCommandList);
		for (String commandName : sortedCommandList) {
			AbstractCommand command = commandHandler.getCommand(commandName);
			System.out.println("* [" + command.getCommand() + "](#" + command.getCommand() + ")");
		}
	}

	private static void readmeCommandDetailsList() {
		CommandHandler commandHandler = new CommandHandler();
		commandHandler.load();
		ArrayList<String> sortedCommandList = new ArrayList<>();
		Collections.addAll(sortedCommandList, commandHandler.getCommands());
		Collections.sort(sortedCommandList);
		for (String commandName : sortedCommandList) {
			AbstractCommand command = commandHandler.getCommand(commandName);

			System.out.println("### " + command.getCommand());
			System.out.println();
			System.out.println(command.getDescription());
			System.out.println();
			if (command.getUsage().length > 0) {
				System.out.println();
				System.out.println("#### Usage");
				System.out.println();
				System.out.println("```php");
				for (String line : command.getUsage()) {
					System.out.println(line);
				}
				System.out.println("```");
			}
		}
	}

}
