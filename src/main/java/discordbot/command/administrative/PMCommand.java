package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !pm
 * make the bot pm someone
 */
public class PMCommand extends AbstractCommand {
	public PMCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Send a message to user";
	}

	@Override
	public String getCommand() {
		return "pm";
	}

	@Override
	public String[] getUsage() {
		return new String[]{"pm <@user> <message..>"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get("command_no_permission");
		}
		if (args.length > 1) {
			if (DisUtil.isUserMention(args[0])) {
				User targetUser = bot.client.getUserById(DisUtil.mentionToId(args[0]));
				if (targetUser != null) {
					String message = "";
					for (int i = 1; i < args.length; i++) {
						message += " " + args[i];
					}
					bot.out.sendPrivateMessage(targetUser, "You got a message from " + author.getAsMention() + ": " + message);
					return Template.get("command_pm_success");
				} else {
					return Template.get("command_pm_cant_find_user");
				}
			} else {
				return Template.get("command_pm_not_a_user");
			}
		}
		return Template.get("command_invalid_use");
	}
}