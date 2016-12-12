package discordbot.command.creator;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 */
public class ConsoleOutputCommand extends AbstractCommand {
	public ConsoleOutputCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Sets the communication channel of the console input";
	}

	@Override
	public String getCommand() {
		return "consolecomm";
	}

	@Override
	public boolean isListed() {
		return true;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"consolecomm connect     //connects to current channel",
				"consolecomm disconnect  //disconnects from current"
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (!bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
			return Template.get(channel, "command_no_permission");
		}
		if (args.length == 0) {
			TextChannel consoleChannel = Launcher.getConsoleChannel();
			if (consoleChannel == null) {
				return "not connected";
			}
			return "Connceted to : " + consoleChannel.getName() + " (" + consoleChannel.getId() + ")";
		}
		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
				case "connect":
					Launcher.setConsoleChannel((TextChannel) channel);
					return "Console set to this channel";
				case "disconnect":
					Launcher.setConsoleChannel(null);
					return "disconnected!";
				default:
					break;
			}
		}
		return "???";
	}
}