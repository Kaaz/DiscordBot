package novaz.command.administrative;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !say
 * make the bot say something
 */
public class PMCommand extends AbstractCommand {
	public PMCommand(NovaBot b) {
		super(b);
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
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isOwner(channel, author)) {
			return TextHandler.get("command_no_permission");
		}
		if (args.length > 1) {
			if (Misc.isUserMention(args[0])) {
				IUser targetUser = bot.instance.getUserByID(Misc.mentionToId(args[0]));
				if (targetUser != null) {
					String message = "";
					for (int i = 1; i < args.length; i++) {
						message += " " + args[i];
					}
					bot.sendPrivateMessage(targetUser, "You got a message from " + author.mention() + ": " + message);
					return TextHandler.get("command_pm_success");
				} else {
					return TextHandler.get("command_pm_cant_find_user");
				}
			} else {
				return TextHandler.get("command_pm_not_a_user");
			}
		}
		return TextHandler.get("command_invalid_use");
	}
}