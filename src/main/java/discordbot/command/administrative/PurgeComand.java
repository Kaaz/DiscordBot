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

import java.util.ArrayList;
import java.util.List;

/**
 * !purge
 * Purges messages in channel
 */
public class PurgeComand extends AbstractCommand {
	private static final int BULK_DELETE_MAX = 100;

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
		Guild guild = ((TextChannel) channel).getGuild();
		boolean hasManageMessages = PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_MANAGE);
		List<Message> messagesToDelete = new ArrayList<>();
		Member toDeleteFrom = null;
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
				channel.getHistory().retrievePast(200).queue(messages -> {
					for (Message message : messages) {
						if (message.isPinned()) {
							continue;
						}
						if ((message.getRawContent().startsWith(cmdPrefix) && hasManageMessages)
								|| (message.getAuthor() == null || message.getAuthor().getId().equals(bot.client.getSelfUser().getId()))) {
							messagesToDelete.add(message);
						}
					}
					deleteBulk(bot, (TextChannel) channel, hasManageMessages, messagesToDelete);
				});
				return "";
			}
			deleteAll = false;
			if (DisUtil.isUserMention(args[0])) {
				toDeleteFrom = guild.getMember(bot.client.getUserById(DisUtil.mentionToId(args[0])));
				if (args.length >= 2 && args[1].matches("^\\d+$")) {
					deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[1]));
				}
			} else if (args[0].toLowerCase().equals("emily")) {
				toDeleteFrom = guild.getSelfMember();
			} else if (args[0].matches("^\\d+$")) {
				deleteAll = true;
				deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[0])) + 1;
			} else {
				toDeleteFrom = DisUtil.findUserIn((TextChannel) channel, args[0]);
				if (args.length >= 2 && args[1].matches("^\\d+$")) {
					deleteLimit = Math.min(deleteLimit, Integer.parseInt(args[1])) + 1;
				}
			}
		}
		if (toDeleteFrom != null && !hasManageMessages && !bot.client.getSelfUser().equals(toDeleteFrom)) {
			return Template.get("permission_missing_manage_messages");
		}
		if (author.equals(toDeleteFrom)) {
			deleteLimit++;//exclude the command itself from the limit
		}
		int finalDeleteLimit = deleteLimit;
		boolean finalDeleteAll = deleteAll;
		Member finalToDeleteFrom = toDeleteFrom;
		channel.getHistory().retrievePast(100).queue(messages -> {
			int deletedCount = 0;
			for (Message msg : messages) {
				if (deletedCount == finalDeleteLimit) {
					break;
				}
				if (msg.isPinned()) {
					continue;
				}
				if (finalDeleteAll && (hasManageMessages || (msg.getAuthor() != null && msg.getAuthor().getId().equals(bot.client.getSelfUser().getId())))) {
					deletedCount++;
					messagesToDelete.add(msg);
				} else if (!finalDeleteAll && finalToDeleteFrom != null && msg.getAuthor() != null && msg.getAuthor().getId().equals(finalToDeleteFrom.getUser().getId())) {
					deletedCount++;
					messagesToDelete.add(msg);
				}
			}
			deleteBulk(bot, (TextChannel) channel, hasManageMessages, messagesToDelete);

		});
		return "";
	}

	/**
	 * Deletes a bunch of messages
	 *
	 * @param bot               the jda instance
	 * @param channel           channel to delete messages in
	 * @param hasManageMessages does the bot have the Permission.MANAGE_CHANNEL for channel
	 * @param messagesToDelete  list of messages to delete
	 */
	private void deleteBulk(DiscordBot bot, TextChannel channel, boolean hasManageMessages, List<Message> messagesToDelete) {
		if (messagesToDelete.isEmpty()) {
			return;
		}
		if (hasManageMessages) {
			bot.out.sendAsyncMessage(channel, Template.get(
					"command_purge_success"), message -> {
				messagesToDelete.add(message);
				for (int index = 0; index < messagesToDelete.size(); index += BULK_DELETE_MAX) {
					if (messagesToDelete.size() - index < 2) {
						messagesToDelete.get(index).deleteMessage();
					} else {
						channel.deleteMessages(messagesToDelete.subList(index, Math.min(index + BULK_DELETE_MAX, messagesToDelete.size())));
					}
					try {
						Thread.sleep(2000L);
					} catch (Exception ignored) {
					}
				}
			});
		} else {
			bot.out.sendAsyncMessage(channel, Template.get("permission_missing_manage_messages"), message -> {
				messagesToDelete.add(message);
				for (Message toDelete : messagesToDelete) {
					toDelete.deleteMessage();
					try {
						Thread.sleep(500L);
					} catch (Exception ignored) {
					}
				}
			});
		}
	}
}