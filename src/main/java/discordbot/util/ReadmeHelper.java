package discordbot.util;

import com.google.common.base.Joiner;
import com.wezinkhof.configuration.ConfigurationBuilder;
import discordbot.core.AbstractCommand;
import discordbot.db.WebDb;
import discordbot.games.AbstractGame;
import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.handler.CommandHandler;
import discordbot.handler.GameHandler;
import discordbot.main.Config;
import discordbot.role.MemberShipRole;
import discordbot.role.RoleRankings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Collection of methods to help me out in maintaining the readme file
 */
public class ReadmeHelper {

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		WebDb.init();
		RoleRankings.init();

		String template = readFile("readme_template.md", StandardCharsets.UTF_8);
		template = template.replace("%_COMMANDS_LIST_SIMPLE_%", readmeCommandSimpleList());
		template = template.replace("%_LIST_OF_GAMES_%", readmeListOfgames());
		template = template.replace("%_LIST_OF_AUTO_RANKS_%", readmeListOfAutoRanks());
		template = template.replace("%_CONFIG_PER_GUILD_%", readmeGuildConfiguration());
		template = template.replace("%_COMMANDS_LIST_DETAILS_%", readmeCommandDetailsList());
		Files.write(Paths.get("./readme.md"), template.getBytes(StandardCharsets.UTF_8));
	}

	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	private static String readmeListOfAutoRanks() {
		String s = "";
		List<MemberShipRole> allRoles = RoleRankings.getAllRoles();
		s += "Name | Time spend |" + Config.EOL;
		s += "--- | --- | " + Config.EOL;
		for (MemberShipRole role : allRoles) {
			s += role.getName() + " | ";
			s += TimeUtil.getRelativeTime((System.currentTimeMillis() + role.getMembershipTime()) / 1000L, false, false) + Config.EOL;
		}

		return s;
	}

	private static String readmeListOfgames() {
		GameHandler gameHandler = new GameHandler(null);
		List<AbstractGame> gameList = gameHandler.getGameList();
		String s = "";
		s += "Key | Name | Players |" + Config.EOL;
		s += "--- | --- | --- |" + Config.EOL;
		for (AbstractGame game : gameList) {
			s += game.getCodeName() + " | ";
			s += game.getFullname() + " | ";
			s += game.getTotalPlayers();
			s += Config.EOL;
		}

		return s;
	}

	private static String readmeGuildConfiguration() {
		String s = "";
		Map<String, AbstractGuildSetting> defaults = DefaultGuildSettings.getDefaults();
		ArrayList<String> skeys = new ArrayList<>(defaults.keySet());
		Collections.sort(skeys);
		s += "Key | Default | Description |" + Config.EOL;
		s += "--- | --- | ---|" + Config.EOL;
		for (String skey : skeys) {

			s += defaults.get(skey).getKey() + " | ";
			s += defaults.get(skey).getDefault() + " | ";
			s += Joiner.on("<br/>").join(defaults.get(skey).getDescription());
			s += Config.EOL;
		}

		return s;
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
		s += "Commands | | | | |" + Config.EOL;
		s += "--- | --- | ---| ---| ---" + Config.EOL;
		int columns = 5;
		int currentColumn = 0;
		for (String commandName : sortedCommandList) {
			AbstractCommand command = commandHandler.getCommand(commandName);
			if (command.isListed() && command.isEnabled()) {
				s += "[" + command.getCommand() + "](#" + command.getCommand() + ")";
				if (currentColumn % columns <= (columns - 2)) {
					s += " | ";
				} else {
					s += Config.EOL;
				}
				currentColumn++;
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
			text += "Accessible though: " + command.getCommand();
			for (String alias : command.getAliases()) {
				text += ", " + alias;
			}
			text += Config.EOL + Config.EOL;
			String visibility;
			switch (command.getVisibility()) {
				case PRIVATE:
					visibility = "in private channels";
					break;
				case PUBLIC:
					visibility = "in public  channels";
					break;
				case BOTH:
					visibility = "in public and private channels";
					break;
				default:
					visibility = "Nowhere";
					break;
			}
			text += "Usable " + visibility + Config.EOL;
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
