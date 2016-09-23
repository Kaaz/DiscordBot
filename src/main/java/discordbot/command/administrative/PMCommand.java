package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !pm
 * make the bot pm someone
 */
public class PMCommand extends AbstractCommand {
	public PMCommand(DiscordBot b) {
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
			return Template.get("command_no_permission");
		}
		if (args.length > 1) {
			if (DisUtil.isUserMention(args[0])) {
				IUser targetUser = bot.instance.getUserByID(DisUtil.mentionToId(args[0]));
				if (targetUser != null) {
					String message = "";
					for (int i = 1; i < args.length; i++) {
						message += " " + args[i];
					}
					bot.out.sendPrivateMessage(targetUser, "You got a message from " + author.mention() + ": " + message);
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