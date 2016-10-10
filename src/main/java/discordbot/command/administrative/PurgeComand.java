package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageList;

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
				"purge               //deletes non-pinned messages",
				"purge @user         //deletes messages from user",
				"purge @user <limit> //deletes up to <limit> messages from user",
				"purge nova          //deletes my messages :("
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"clear",
				"delete"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		boolean hasManageMessages = channel.getModifiedPermissions(bot.client.getOurUser()).contains(Permissions.MANAGE_MESSAGES);
		IUser toDeleteFrom = null;
		int deleteLimit = 500;
		boolean deleteAll = true;
		if (args.length >= 1) {
			deleteAll = false;
			if (DisUtil.isUserMention(args[0])) {
				if (!hasManageMessages) {
					return Template.get("permission_missing_manage_messages");
				}
				toDeleteFrom = bot.client.getUserByID(DisUtil.mentionToId(args[0]));
			} else if (args[0].toLowerCase().equals("nova")) {
				toDeleteFrom = bot.client.getOurUser();
			}
			if (args.length >= 2 && args[1].matches("^\\d+$")) {
				deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[1]));
			}
		}
		if (!bot.isOwner(channel, author) && !bot.client.getOurUser().equals(author)) {
			return Template.get("command_invalid_use");
		}
		int deletedCount = 0;
		for (IMessage msg : new MessageList(bot.client, channel, 500)) {
			if (deletedCount == deleteLimit) {
				break;
			}
			if (msg.isPinned()) {
				continue;
			}
			if (deleteAll && (hasManageMessages || msg.getAuthor().equals(bot.client.getOurUser()))) {
				deletedCount++;
				bot.out.deleteMessage(msg);
			} else if (!deleteAll && toDeleteFrom != null && msg.getAuthor().equals(toDeleteFrom)) {
				deletedCount++;
				bot.out.deleteMessage(msg);
			}
		}
		if (hasManageMessages) {
			return Template.get("command_purge_success");
		}
		return Template.get("permission_missing_manage_messages");
	}
}