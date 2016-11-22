package discordbot.command.creator;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CRank;
import discordbot.db.controllers.CUser;
import discordbot.db.controllers.CUserRank;
import discordbot.db.model.ORank;
import discordbot.db.model.OUser;
import discordbot.db.model.OUserRank;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * !userrank
 */
public class UserRankCommand extends AbstractCommand {

	public UserRankCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "This command is intended for bot admins";
	}

	@Override
	public String getCommand() {
		return "userrank";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"userrank <user>                   //check rank of user",
				"userrank <user> <rank>            //gives a rank to user",
				"userrank <user> perm <+/-> <node> //adds/removes permission from user",
				"userrank permlist                 //lists all permissions",
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
		SimpleRank authorRank = bot.security.getSimpleRank(author);
		if (!authorRank.isAtLeast(SimpleRank.BOT_ADMIN)) {
			return Template.get("no_permission");
		}
		if (args.length >= 1) {
			if (args[0].equals("permlist")) {
				return "Available permissions: " + Config.EOL +
						tableFor(Arrays.asList(OUser.PermissionNode.values()));
			}
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
			SimpleRank targetSimpleRank = bot.security.getSimpleRank(user);
			OUser dbUser = CUser.findBy(user.getId());
			if (args.length == 1) {
				OUserRank userRank = CUserRank.findBy(user.getId());
				if (userRank.rankId == 0 && !targetSimpleRank.isAtLeast(SimpleRank.CREATOR)) {
					return Template.get("command_userrank_no_rank", user.getUsername());
				} else if (targetSimpleRank.isAtLeast(SimpleRank.CREATOR)) {
					return Template.get("command_userrank_rank", user.getUsername(), "creator");
				} else {
					return Template.get("command_userrank_rank", user.getUsername(), CRank.findById(userRank.rankId).codeName);
				}
			} else if (args.length == 2 && !args[1].equals("perm")) {
				ORank newRank = CRank.findBy(args[1]);
				if (newRank.id == 0) {
					return Template.get("command_userrank_rank_not_exists", args[1]);
				}
				OUserRank userRank = CUserRank.findBy(CUser.getCachedId(user.getId(), user.getUsername()));
				userRank.rankId = newRank.id;
				CUserRank.insertOrUpdate(userRank);
				return Template.get("command_userrank_rank", user.getUsername(), newRank.codeName);
			}

			if (args[1].equals("perm")) {

				if (args.length < 4) {
					if (dbUser.getPermission().isEmpty()) {
						return "No permissions set for " + user.getUsername();
					}
					return "Permissions for " + user.getUsername() + Config.EOL +
							tableFor(dbUser.getPermission());
				}
				boolean adding = true;
				switch (args[2].toLowerCase()) {
					case "-":
					case "del":
					case "rem":
					case "min":
					case "remove":
					case "delete":
						adding = false;
						break;
				}
				try {
					OUser.PermissionNode node = OUser.PermissionNode.valueOf(args[3].toUpperCase());
					if (adding) {
						dbUser.addPermission(node);
						CUser.update(dbUser);
						return String.format(":+1: adding `%s` to %s", node.toString(), user.getUsername());
					}
					dbUser.removePermission(node);
					CUser.update(dbUser);
					return String.format(":+1: removed `%s` from %s", node.toString(), user.getUsername());
				} catch (Exception e) {
					return "Invalid permission node";
				}
			}
		}
		return Template.get("command_invalid_use");
	}

	private String tableFor(Collection<OUser.PermissionNode> nodes) {
		List<List<String>> tbl = new ArrayList<>();
		for (OUser.PermissionNode node : nodes) {
			tbl.add(Arrays.asList(node.toString(), node.getDescription()));
		}
		return Misc.makeAsciiTable(Arrays.asList("code", "description"), tbl, null);
	}
}