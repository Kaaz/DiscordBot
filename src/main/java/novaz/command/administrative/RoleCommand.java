package novaz.command.administrative;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

import java.util.EnumSet;
import java.util.List;

/**
 * !role
 * manages roles
 */
public class RoleCommand extends AbstractCommand {
	public RoleCommand(NovaBot b) {
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
				"role add @user <role>    //adds role to user",
				"role remove @user <role> //remove role from user",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public boolean isAllowedInPrivateChannel() {
		return false;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isOwner(channel, author)) {
			return TextHandler.get("command_no_permission");
		}

		if (args.length == 0 || args[0].equals("list")) {
			String out = "I found the following roles" + Config.EOL;
			List<IRole> roles = channel.getGuild().getRoles();
			for (IRole role : roles) {
				out += String.format("%s (%s)" + Config.EOL, role.getName(), role.getID());
				EnumSet<Permissions> permissions = role.getPermissions();
//				out += "`";
//				for (Permissions permission : permissions) {
//					out += " " + permission.toString();
//				}
//				out += "`" + Config.EOL;
			}
			return out;
		}
		return ":face_palm: I expected you to know how to use it";
	}
}