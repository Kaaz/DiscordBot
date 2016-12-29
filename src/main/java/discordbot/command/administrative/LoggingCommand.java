package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * command to modify the logging settings of a guild
 */
public class LoggingCommand extends AbstractCommand {
	public LoggingCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "log all the things! Configure how/where/what is being logged";
	}

	@Override
	public String getCommand() {
		return "logging";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"log"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);
		return Template.get("command_no_permission");
	}
}