package novaz.command.administrative;

import novaz.command.CommandVisibility;
import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;

/**
 * Created on 11-8-2016
 */
public class CustomCommand extends AbstractCommand {
	private String[] valid_actions = {"add", "delete"};

	public CustomCommand(NovaBot b) {
		super(b);
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
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isOwner(channel, author)) {
			return TextHandler.get("permission_denied");
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
				bot.addCustomCommand(channel.getGuild(), args[1], output.trim());
				return "Added !" + args[1];
			} else if (args[0].equals("delete")) {
				bot.removeCustomCommand(channel.getGuild(), args[1]);
				return "Removed !" + args[1];
			}
		} else if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
			return "All custom commands: " + Config.EOL + Misc.makeTable(Arrays.asList(bot.commands.getCustomCommands()));
		} else {
			return getDescription();
		}
		return TextHandler.get("permission_denied");
	}
}
