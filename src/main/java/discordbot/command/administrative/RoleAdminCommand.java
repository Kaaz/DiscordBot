package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CGuildRoleAssignable;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.role.RoleRankings;
import discordbot.util.DisUtil;
import discordbot.util.Misc;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.List;

/**
 * !role
 * manages roles
 */
public class RoleAdminCommand extends AbstractCommand {
	public RoleAdminCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Management of roles & general permissions " + Config.EOL +
				"You can give users the ability to self-assign roles. " + Config.EOL +
				"Users can get/remove their own roles with the `getrole` command ";
	}

	@Override
	public String getCommand() {
		return "roleadmin";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"You can specify which roles are self-assignable by users with the following commands: ",
				"",
				"roleadmin self                   //check what roles are self-assignable",
				"roleadmin self add <role>        //add a role to the list of assignable roles",
				"roleadmin self remove <role>     //remove a role from the list of assignable roles",
				"roleadmin self describe <role>   //add a description to what this role does",

				"",
				"",
				"roleadmin                        //lists roles",
				"roleadmin cleanup                //cleans up the roles from the time-based rankings",
				"roleadmin setup                  //creates the roles for the time-based rankings",
//				"roleadmin bind BOT_ROLE <discordrole> //binds a discordrole to a botrole",
//				"roleadmin add @user <role>            //adds role to user",
//				"roleadmin remove @user <role>         //remove role from user",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"ra"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get("command_no_permission");
		}
		if (args.length == 0 || args[0].equals("list")) {
			String out = "I found the following roles" + Config.EOL;
			List<Role> roles = guild.getRoles();
			for (Role role : roles) {
				if (role.getPosition() == -1) {
					continue;
				}
				out += String.format("%s (%s)" + Config.EOL, role.getName(), role.getId());
			}
			return out;
		}
		switch (args[0].toLowerCase()) {
			case "self":
				if (!PermissionUtil.checkPermission(guild, bot.client.getSelfInfo(), Permission.MANAGE_ROLES)) {
					return Template.get("permission_missing_manage_roles");
				}
				if (args.length == 1) {
					return "self roles overview";
				}
				if (args.length < 3) {
					return Template.get("command_invalid_use");
				}
				String roleName = Misc.joinStrings(args, 2);
				Role role = DisUtil.findRole(guild, roleName);
				if (role == null) {
					return "role not found";
				}
				switch (args[1].toLowerCase()) {
					case "add":
					case "+":
						CGuildRoleAssignable.insertOrUpdate(CGuild.getCachedId(guild.getId()), role.getId(), role.getName());
						return "adding";
					case "remove":
					case "-":
						CGuildRoleAssignable.delete(CGuild.getCachedId(guild.getId()), role.getId());
						return "removing";
					case "describe":
				}
			case "cleanup":
				RoleRankings.cleanUpRoles(guild, bot.client.getSelfInfo());
				return "Removed all the time-based roles";
			case "setup":
				if (RoleRankings.canModifyRoles(guild, bot.client.getSelfInfo())) {
					RoleRankings.fixForServer(guild);
					return "Set up all the required roles :smile:";
				}
				return "No permissions to manage roles";
			default:
				return ":face_palm: I expected you to know how to use it";
		}
	}
}