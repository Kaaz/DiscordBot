package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * command for kicking users from a guild
 */
public class KickCommand extends AbstractCommand {
	public KickCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "kicks a user";
	}

	@Override
	public String getCommand() {
		return "kick";
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
		SimpleRank rank = bot.security.getSimpleRank(author);
		return Template.get("command_no_permission");
	}
}