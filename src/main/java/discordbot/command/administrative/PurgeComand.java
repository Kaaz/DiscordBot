package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.List;

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
		return "deletes non-pinned messages";
	}

	@Override
	public String getCommand() {
		return "purge";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"purge               //deletes up to 100 messages",
				"purge <limit>       //deletes non-pinned messages",
				"purge @user         //deletes messages from user",
				"purge @user <limit> //deletes up to <limit> messages from user",
				"purge emily         //deletes my messages :("
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
	public String execute(String[] args, MessageChannel channel, User author) {
		boolean hasManageMessages = PermissionUtil.checkPermission((TextChannel) channel, bot.client.getSelfInfo(), Permission.MESSAGE_MANAGE);
		User toDeleteFrom = null;
		int deleteLimit = 100;
		boolean deleteAll = true;
		if (!bot.isOwner(channel, author) && !bot.client.getSelfInfo().equals(author)) {
			return Template.get("command_invalid_use");
		}
		if (args.length >= 1) {
			deleteAll = false;
			if (DisUtil.isUserMention(args[0])) {
				toDeleteFrom = bot.client.getUserById(DisUtil.mentionToId(args[0]));
				if (args.length >= 2 && args[1].matches("^\\d+$")) {
					deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[1]));
				}
			} else if (args[0].toLowerCase().equals("emily")) {
				toDeleteFrom = bot.client.getSelfInfo();
			} else if (args[0].matches("^\\d+$")) {
				deleteAll = true;
				deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[0]));
			} else {
				toDeleteFrom = DisUtil.findUserIn((TextChannel) channel, args[0]);
			}
		}
		if (toDeleteFrom != null && !hasManageMessages && !bot.client.getSelfInfo().equals(toDeleteFrom)) {
			return Template.get("permission_missing_manage_messages");
		}
		int deletedCount = 0;
		List<Message> retrieve = channel.getHistory().retrieve(100);
		for (Message msg : retrieve) {
			if (deletedCount == deleteLimit) {
				break;
			}
			if (msg.isPinned()) {
				continue;
			}
			if (deleteAll && (hasManageMessages || msg.getAuthor().getId().equals(bot.client.getSelfInfo().getId()))) {
				deletedCount++;
				bot.out.deleteMessage(msg);
			} else if (!deleteAll && toDeleteFrom != null && msg.getAuthor().getId().equals(toDeleteFrom.getId())) {
				deletedCount++;
				bot.out.deleteMessage(msg);
			}
		}
		if (hasManageMessages) {
			bot.out.sendAsyncMessage(channel, Template.get("command_purge_success"), message -> {
				bot.out.deleteMessage(message);
			});
			return "";
		}
		return Template.get("permission_missing_manage_messages");
	}
}