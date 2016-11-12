package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

/**
 * !purge
 * Purges messages in channel
 */
public class PurgeComand extends AbstractCommand {
	public PurgeComand() {
		super();
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
				"purge commands      //delete command related messages",
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		TextChannel chan = ((TextChannel) channel);
		Guild guild = chan.getGuild();
		boolean hasManageMessages = PermissionUtil.checkPermission(chan, guild.getMember(bot.client.getSelfUser()), Permission.MESSAGE_MANAGE);
		User toDeleteFrom = null;
		int deleteLimit = 100;
		boolean deleteAll = true;
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN) && !bot.client.getSelfUser().equals(author)) {
			return Template.get("no_permission");
		}
		if (args.length >= 1) {
			if (args[0].equals("commands") || args[0].equals("command")) {
				if (!hasManageMessages) {
					Template.get("permission_missing_manage_messages");
				}
				String cmdPrefix = DisUtil.getCommandPrefix(channel);
				List<Message> retrieve = channel.getHistory().retrievePast(100).block();
				for (Message message : retrieve) {
					if (message.isPinned()) {
						continue;
					}
					if (message.getAuthor().getId().equals(bot.client.getSelfUser().getId()) ||
							message.getRawContent().startsWith(cmdPrefix)) {
						bot.out.deleteMessage(message);
					}
				}
				bot.out.sendAsyncMessage(channel, Template.get("command_purge_success"), message -> bot.out.deleteMessage(message));
				return "";
			}
			deleteAll = false;
			if (DisUtil.isUserMention(args[0])) {
				toDeleteFrom = bot.client.getUserById(DisUtil.mentionToId(args[0]));
				if (args.length >= 2 && args[1].matches("^\\d+$")) {
					deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[1]));
				}
			} else if (args[0].toLowerCase().equals("emily")) {
				toDeleteFrom = bot.client.getSelfUser();
			} else if (args[0].matches("^\\d+$")) {
				deleteAll = true;
				deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[0]));
			} else {
				toDeleteFrom = DisUtil.findUserIn((TextChannel) channel, args[0]);
				if (args.length >= 2 && args[1].matches("^\\d+$")) {
					deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[1]));
				}
			}
		}
		if (toDeleteFrom != null && !hasManageMessages && !bot.client.getSelfUser().equals(toDeleteFrom)) {
			return Template.get("permission_missing_manage_messages");
		}
		if (author.equals(toDeleteFrom)) {
			deleteLimit++;//exclude the command itself from the limit
		}
		int deletedCount = 0;
		List<Message> retrieve = channel.getHistory().retrievePast(100).block();
		for (Message msg : retrieve) {
			if (deletedCount == deleteLimit) {
				break;
			}
			if (msg.isPinned()) {
				continue;
			}
			if (deleteAll && (hasManageMessages || msg.getAuthor().getId().equals(bot.client.getSelfUser().getId()))) {
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