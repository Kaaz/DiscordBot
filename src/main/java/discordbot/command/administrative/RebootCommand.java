package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.core.ExitCode;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.UpdateUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !reboot
 * restarts the bot
 */
public class RebootCommand extends AbstractCommand {
	public RebootCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "restarts the bot";
	}

	@Override
	public String getCommand() {
		return "reboot";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"reboot         //reboots the system",
				"reboot update  //reboots the system and updates"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{"restart"};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.isOwner(channel, author) || author.getID().equals("158310004187725824")) {
			if (args.length > 0 && args[0].equalsIgnoreCase("update") && UpdateUtil.getLatestVersion().isHigherThan(Launcher.getVersion())) {
				bot.out.sendMessage(channel, "There is an update! Updating :arrows_counterclockwise:");
				Launcher.stop(ExitCode.UPDATE);
			}
			bot.out.sendMessage(channel, "Rebooting in about a minute :smile:");
			Launcher.stop(ExitCode.REBOOT);
		}
		return Template.get("command_no_permission");
	}
}