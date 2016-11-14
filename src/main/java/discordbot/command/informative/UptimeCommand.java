package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

public class UptimeCommand extends AbstractCommand {
	public UptimeCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "How long am I running for?";
	}

	@Override
	public String getCommand() {
		return "uptime";
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		return Template.get(channel, "command_uptime_upfor", TimeUtil.getRelativeTime(bot.startupTimeStamp, false));
	}
}