package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.core.ExitCode;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.UpdateUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

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
	public String execute(String[] args, MessageChannel channel, User author) {
		if (bot.isOwner(channel, author) || author.getId().equals("158310004187725824")) {
			if (args.length > 0 && args[0].equalsIgnoreCase("update") && UpdateUtil.getLatestVersion().isHigherThan(Launcher.getVersion())) {
				bot.out.sendAsyncMessage(channel, Template.get("command_reboot_update"), null);
				Launcher.stop(ExitCode.UPDATE);
			} else if (args.length > 0 && args[0].equals("forceupdate")) {
				bot.out.sendAsyncMessage(channel, Template.get("command_reboot_forceupdate"), null);
				Launcher.stop(ExitCode.UPDATE);
			}
			bot.out.sendAsyncMessage(channel, Template.get("command_reboot_success"), null);
			Launcher.stop(ExitCode.REBOOT);
		}
		return Template.get("command_no_permission");
	}
}