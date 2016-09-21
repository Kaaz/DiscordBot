package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.TextHandler;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

/**
 * !purge
 * Purges messages in channel
 */
public class PurgeComand extends AbstractCommand {
	public PurgeComand(DiscordBot b) {
		super(b);
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String getDescription() {
		return "purges messages";
	}

	@Override
	public String getCommand() {
		return "purge";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"purge       //deletes non-pinned messages",
				"purge @user //deletes messages from user",
				"purge nova  //deletes my messages :("
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"clear"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		boolean hasManageMessages = channel.getModifiedPermissions(bot.instance.getOurUser()).contains(Permissions.MANAGE_MESSAGES);
		IUser toDeleteFrom = null;
		boolean deleteAll = true;
		if (args.length >= 1) {
			deleteAll = false;
			if (DisUtil.isUserMention(args[0])) {
				if (!hasManageMessages) {
					return TextHandler.get("permission_missing_manage_messages");
				}
				toDeleteFrom = bot.instance.getUserByID(DisUtil.mentionToId(args[0]));
			} else if (args[0].toLowerCase().equals("nova")) {
				toDeleteFrom = bot.instance.getOurUser();
			}
		}
		if (!bot.isOwner(channel, author) && !bot.instance.getOurUser().equals(author)) {
			return TextHandler.get("command_invalid_use");
		}
		boolean finalDeleteAll = deleteAll;
		IUser finalToDeleteFrom = toDeleteFrom;
		channel.getMessages().stream().filter(msg -> !msg.isPinned()).forEach(
				msg -> {
					if (finalDeleteAll && (hasManageMessages || msg.getAuthor().equals(bot.instance.getOurUser()))) {
						bot.out.deleteMessage(msg);
					} else if (!finalDeleteAll && finalToDeleteFrom != null && msg.getAuthor().equals(finalToDeleteFrom)) {
						bot.out.deleteMessage(msg);
					}
				});
		if (hasManageMessages) {
			return TextHandler.get("command_purge_success");
		}
		return TextHandler.get("permission_missing_manage_messages");
	}
}