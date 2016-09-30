package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.role.RoleRankings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

/**
 * !role
 * manages roles
 */
public class RoleCommand extends AbstractCommand {
	public RoleCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Role";
	}

	@Override
	public String getCommand() {
		return "role";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"role                     //lists roles",
				"role list                //lists roles",
				"role cleanup             //cleans up the roles from the time-based rankings",
				"role setup               //creates the roles for the time-based rankings",
				"role add @user <role>    //adds role to user",
				"role remove @user <role> //remove role from user",
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
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isOwner(channel, author)) {
			return Template.get("command_no_permission");
		}
		if (args.length == 0 || args[0].equals("list")) {
			String out = "I found the following roles" + Config.EOL;
			List<IRole> roles = channel.getGuild().getRoles();
			for (IRole role : roles) {
				if (role.isEveryoneRole()) {
					continue;
				}
				out += String.format("%s (%s)" + Config.EOL, role.getName(), role.getID());
			}
			return out;
		}
		switch (args[0]) {
			case "cleanup":
				try {
					RoleRankings.cleanUpRoles(channel.getGuild(), bot.instance.getOurUser());
				} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
					return "Tried cleaning up but this happened: " + e.getMessage();
				}
				return "Removed all the time-based roles";
			case "setup":
				if (RoleRankings.canModifyRoles(channel.getGuild(), bot.instance.getOurUser())) {
					RoleRankings.fixForServer(channel.getGuild());
					return "Set up all the required roles :smile:";
				}
				return "No permissions to manage roles";
			default:
				return ":face_palm: I expected you to know how to use it";
		}
	}
}