package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * ban a user from a guild
 */
public class BanCommand extends AbstractCommand {
	public BanCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Ban those nasty humans";
	}

	@Override
	public String getCommand() {
		return "ban";
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