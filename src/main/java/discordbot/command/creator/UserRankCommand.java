package discordbot.command.creator;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CRank;
import discordbot.db.controllers.CUser;
import discordbot.db.controllers.CUserRank;
import discordbot.db.model.ORank;
import discordbot.db.model.OUserRank;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !userrank
 */
public class UserRankCommand extends AbstractCommand {

	public UserRankCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "User Ranks!";
	}

	@Override
	public String getCommand() {
		return "userrank";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"userrank <user>        //list of tags",
				"userrank <user> <rank> //shows your tags"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"ur"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (!bot.security.getSimpleRank(author).isAtLeast(SimpleRank.CREATOR)) {
			return Template.get("no_permission");
		}
		if (args.length >= 1) {
			User user;
			if (DisUtil.isUserMention(args[0])) {
				user = bot.client.getUserById(DisUtil.mentionToId(args[0]));
			} else if (args[0].matches("^i\\d+$")) {
				user = bot.client.getUserById(CUser.getCachedDiscordId(Integer.parseInt(args[0].substring(1))));
			} else {
				user = DisUtil.findUserIn((TextChannel) channel, args[0]);
			}
			if (user == null) {
				return Template.get("cant_find_user", args[0]);
			}
			if (args.length == 1) {
				OUserRank userRank = CUserRank.findBy(user.getId());
				if (userRank.rankId == 0 && !bot.isCreator(user)) {
					return Template.get("command_userrank_no_rank", user.getUsername());
				} else if (bot.isCreator(user)) {
					return Template.get("command_userrank_rank", user.getUsername(), "creator");
				} else {
					return Template.get("command_userrank_rank", user.getUsername(), CRank.findById(userRank.rankId).codeName);
				}
			} else if (args.length == 2) {
				ORank rank = CRank.findBy(args[1]);
				if (rank.id == 0) {
					return Template.get("command_userrank_rank_not_exists", args[1]);
				}
				OUserRank userRank = CUserRank.findBy(CUser.getCachedId(user.getId(), user.getUsername()));
				userRank.rankId = rank.id;
				CUserRank.insertOrUpdate(userRank);
				return Template.get("command_userrank_rank", user.getUsername(), rank.codeName);
			}
		}
		return Template.get("command_invalid_use");
	}
}