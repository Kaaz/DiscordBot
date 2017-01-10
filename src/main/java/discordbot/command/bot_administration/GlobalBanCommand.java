package discordbot.command.bot_administration;

import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OUser;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * ban a user from a guild
 */
public class GlobalBanCommand extends AbstractCommand {
	public GlobalBanCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Ban those nasty humans";
	}

	@Override
	public String getCommand() {
		return "globalban";
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
		if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 1) {
			boolean unban = args.length > 1 && Misc.isFuzzyFalse(args[1]);
			OUser user = CUser.findBy(args[0]);
			user.banned = unban ? 0 : 1;
			if (user.id == 0) {
				return "User `" + args[0] + "` not found";
			}
			CUser.update(user);
			if (unban) {
				bot.security.removeUserBan(user.discord_id);
				return "`" + user.name + "` (`" + user.discord_id + "`) has been globally unbanned";
			} else {
				bot.security.addUserBan(user.discord_id);
				return "`" + user.name + "` (`" + user.discord_id + "`) has been globally banned";
			}
		}
		return Template.get("command_no_permission");
	}
}