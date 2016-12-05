package discordbot.command.informative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CGuildRoleAssignable;
import discordbot.db.model.OGuildRoleAssignable;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import discordbot.util.Misc;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.List;


/**
 * !getrole
 * gives a role to a user, or takes it away
 */
public class GetroleCommand extends AbstractCommand {

	public GetroleCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "allows users to request a role";
	}

	@Override
	public String getCommand() {
		return "getrole";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"list                //see what roles are available",
				"remove <rolename>   //removes the <rolename> from you",
				"<rolename>          //assign the <rolename> to you ",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		if (!PermissionUtil.checkPermission(guild, bot.client.getSelfInfo(), Permission.MANAGE_ROLES)) {
			return Template.get("permission_missing_manage_roles");
		}
		if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
			List<OGuildRoleAssignable> roles = CGuildRoleAssignable.getRolesFor(CGuild.getCachedId(guild.getId()));
			if (roles.isEmpty()) {
				return Template.get("command_getrole_empty");
			}
			String ret = "You can request the following roles:" + Config.EOL + Config.EOL;
			for (OGuildRoleAssignable role : roles) {
				ret += "`" + role.roleName + "`" + Config.EOL;
				if (!role.description.isEmpty()) {
					ret += " -> " + role.description + Config.EOL;
				}
				ret += Config.EOL;
			}
			return ret;
		}
		int startIndex = 0;
		boolean isAdding = true;
		if (args[0].equals("remove")) {
			isAdding = false;
			startIndex = 1;
		}
		if (startIndex >= args.length) {
			return Template.get("command_invalid_use");
		}
		String roleName = Misc.joinStrings(args, startIndex);
		Role role = DisUtil.findRole(guild, roleName);
		if (role == null) {
			return Template.get("command_getrole_not_assignable");
		}
		OGuildRoleAssignable roleAssignable = CGuildRoleAssignable.findBy(CGuild.getCachedId(guild.getId()), role.getId());
		if (roleAssignable.guildId == 0) {
			return Template.get("command_getrole_not_assignable");
		}
		if (isAdding) {
			bot.out.addRole(author, role);
			if (guild.getRolesForUser(author).contains(role)) {
				return Template.get("command_getrole_not_assigned", role.getName());
			}
			return Template.get("command_getrole_assigned", role.getName());
		}
		if (!guild.getRolesForUser(author).contains(role)) {
			return Template.get("command_getrole_not_removed");
		}
		bot.out.removeRole(author, role);
		return Template.get("command_getrole_removed");
	}
}