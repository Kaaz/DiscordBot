package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.CommandHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

import java.util.Arrays;

/**
 * Created on 11-8-2016
 */
public class CustomCommand extends AbstractCommand {
	private String[] valid_actions = {"add", "delete"};

	public CustomCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Add and remove custom commands";
	}

	@Override
	public String getCommand() {
		return "command";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"command add <command> <action>  //adds a command",
				"command delete <command>        //deletes a command",
				"command list                    //shows a list of existing custom commands"

		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.BOTH;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (!bot.isOwner(channel, author)) {
			return Template.get("permission_denied");
		}
		if (args.length >= 2 && Arrays.asList(valid_actions).contains(args[0])) {
			if (args[0].equals("add") && args.length > 2) {
				String output = "";
				for (int i = 2; i < args.length; i++) {
					output += args[i] + " ";
				}
				if (args[0].startsWith("!")) {
					args[0] = args[0].substring(1);
				}
				CommandHandler.addCustomCommand(args[1], output.trim());
				return "Added !" + args[1];
			} else if (args[0].equals("delete")) {
				CommandHandler.removeCustomCommand(args[1]);
				return "Removed !" + args[1];
			}
		} else if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
			return "All custom commands: " + Config.EOL + Misc.makeTable(Arrays.asList(CommandHandler.getCustomCommands()));
		} else {
			return getDescription();
		}
		return Template.get("permission_denied");
	}
}
