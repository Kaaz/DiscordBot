package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.role.RoleRankings;
import net.dv8tion.jda.core.entities.*;

import java.util.List;

/**
 * !role
 * manages roles
 */
public class RoleCommand extends AbstractCommand {
	public RoleCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Management of roles";
	}

	@Override
	public String getCommand() {
		return "role";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"role                             //lists roles",
				"role list                        //lists roles",
				"role cleanup                     //cleans up the roles from the time-based rankings",
				"role setup                       //creates the roles for the time-based rankings",
				"role bind BOT_ROLE <discordrole> //binds a discordrole to a botrole",
				"role add @user <role>            //adds role to user",
				"role remove @user <role>         //remove role from user",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"roles"
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
		switch (args[0]) {
			case "cleanup":
				RoleRankings.cleanUpRoles(guild, bot.client.getSelfUser());
				return "Removed all the time-based roles";
			case "setup":
				if (RoleRankings.canModifyRoles(guild, bot.client.getSelfUser())) {
					RoleRankings.fixForServer(guild);
					return "Set up all the required roles :smile:";
				}
				return "No permissions to manage roles";
			default:
				return ":face_palm: I expected you to know how to use it";
		}
	}
}