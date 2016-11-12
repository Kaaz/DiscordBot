package discordbot.command.bot_administration;

import discordbot.core.AbstractCommand;
import discordbot.core.ExitCode;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.permission.SimpleRank;
import discordbot.util.UpdateUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !reboot
 * restarts the bot
 */
public class RebootCommand extends AbstractCommand {
	public RebootCommand() {
		super();
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
			if (args.length > 0 && args[0].equalsIgnoreCase("update") && UpdateUtil.getLatestVersion().isHigherThan(Launcher.getVersion())) {
				bot.out.sendAsyncMessage(channel, Template.get("command_reboot_update"), message -> {
					Launcher.stop(ExitCode.UPDATE);
				});
			} else if (args.length > 0 && args[0].equals("forceupdate") || args[0].equals("fursupdate")) {
				bot.out.sendAsyncMessage(channel, Template.get("command_reboot_forceupdate"), message -> {
					Launcher.stop(ExitCode.UPDATE);
				});
			}
			bot.out.sendAsyncMessage(channel, Template.get("command_reboot_success"), message -> {
				Launcher.stop(ExitCode.REBOOT);
			});
		}
		return Template.get("command_no_permission");
	}
}