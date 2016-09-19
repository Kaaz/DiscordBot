package novaz.util;

import com.wezinkhof.configuration.ConfigurationBuilder;
import novaz.core.AbstractCommand;
import novaz.db.WebDb;
import novaz.handler.CommandHandler;
import novaz.main.Config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Collection of methods to help me out in maintaining the readme file
 */
public class ReadmeHelper {

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		WebDb.init();

		String template = readFile("readme_template.md", StandardCharsets.UTF_8);
		template = template.replace("%_COMMANDS_LIST_SIMPLE_%", readmeCommandSimpleList());
		template = template.replace("%_COMMANDS_LIST_DETAILS_%", readmeCommandDetailsList());
		Files.write(Paths.get("./readme.md"), template.getBytes(StandardCharsets.UTF_8));
	}

	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
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
			if (command.isListed() && command.isEnabled()) {
				s += "* [" + command.getCommand() + "](#" + command.getCommand() + ")" + Config.EOL;
			}
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
			if (!command.isEnabled() || !command.isListed()) {
				continue;
			}
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
