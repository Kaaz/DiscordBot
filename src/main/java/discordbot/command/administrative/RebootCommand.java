package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.core.ExitCode;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !exit
 * completely stops the program
 */
public class RebootCommand extends AbstractCommand {
	public RebootCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "completely shuts the bot down";
	}

	@Override
	public String getCommand() {
		return "exit";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.isOwner(channel, author)) {
			bot.out.sendMessage(channel, "Rebooting in about a minute :smile:");
			System.exit(ExitCode.REBOOT.getCode());
		}
		return Template.get("command_no_permission");
	}
}