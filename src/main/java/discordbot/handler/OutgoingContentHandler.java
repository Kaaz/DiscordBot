package discordbot.handler;

import discordbot.handler.discord.RoleModifyTask;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class OutgoingContentHandler {
	private final static long DELETE_INTERVAL = 500L;
	private final DiscordBot bot;
	private final MessageDeleter deleteThread;
	private final RoleModifier roleThread;

	public OutgoingContentHandler(DiscordBot b) {
		bot = b;
		deleteThread = new MessageDeleter();
		roleThread = new RoleModifier();
	}

	/**
	 * @param channel  channel to send to
	 * @param content  the message
	 * @param callback
	 * @return IMessage or null
	 */
	public void sendAsyncMessage(MessageChannel channel, String content, Consumer<Message> callback) {
		channel.sendMessageAsync(content, callback);
	}

	public Message sendMessage(MessageChannel channel, String content) {
		return channel.sendMessage(content);
	}

	/**
	 * adds a role to a user
	 *
	 * @param user the user
	 * @param role the role
	 */
	public void addRole(User user, Role role) {
		roleThread.offer(new RoleModifyTask(user, role, true));
	}

	/**
	 * removes a role from a user
	 *
	 * @param user the user
	 * @param role the role
	 */
	public void removeRole(User user, Role role) {
		roleThread.offer(new RoleModifyTask(user, role, false));
	}

	/**
	 * Sends an error to the Config.CREATOR_ID
	 *
	 * @param error        the Exception
	 * @param extradetails extra details about the error
	 */
	public void sendErrorToMe(Exception error, Object... extradetails) {
		String errorMessage = "I'm sorry to inform you that I've encountered a **" + error.getClass().getName() + "**" + Config.EOL;
		errorMessage += "Message: " + Config.EOL;
		errorMessage += error.getLocalizedMessage() + Config.EOL;
		String stack = "";
		int maxTrace = 6;
		StackTraceElement[] stackTrace1 = error.getStackTrace();
		for (int i = 0; i < stackTrace1.length; i++) {
			StackTraceElement stackTrace = stackTrace1[i];
			stack += stackTrace.toString() + Config.EOL;
			if (i > maxTrace) {
				break;
			}
		}
		errorMessage += "Accompanied stacktrace: " + Config.EOL + Misc.makeTable(stack) + Config.EOL;
		if (extradetails.length > 0) {
			errorMessage += "Extra information: " + Config.EOL;
			for (int i = 1; i < extradetails.length; i += 2) {
				if (extradetails[i] != null) {
					errorMessage += extradetails[i - 1] + " = " + extradetails[i] + Config.EOL;
				} else if (extradetails[i - 1] != null) {
					errorMessage += extradetails[i - 1];
				}
			}
		}
		sendPrivateMessage(bot.client.getUserById(Config.CREATOR_ID), errorMessage);
	}

	public void sendMessageToCreator(String message) {
		sendPrivateMessage(bot.client.getUserById(Config.CREATOR_ID), message);
	}

	/**
	 * Sends a private message to user
	 *
	 * @param target  the user to send it to
	 * @param message the message
	 */
	public void sendPrivateMessage(User target, String message) {
		target.getPrivateChannel().sendMessageAsync(message, null);
	}

	/**
	 * Puts a message in the delete queue
	 *
	 * @param message the message to delete
	 */
	public void deleteMessage(Message message) {
		deleteThread.offer(message);
	}

	/**
	 * simple thread to delete messages, since it bugs out otherwise
	 */
	private class MessageDeleter extends Thread {
		private LinkedBlockingQueue<Message> itemsToDelete = new LinkedBlockingQueue<>();
		private volatile boolean processTerminated = false;

		MessageDeleter() {
			start();
		}

		public void run() {
			try {
				while (!Launcher.killAllThreads) {
					final Message msgToDelete = itemsToDelete.take();
					if (msgToDelete != null) {
						msgToDelete.deleteMessage();
					}
					Thread.sleep(DELETE_INTERVAL);
				}
			} catch (InterruptedException ignored) {
			} finally {
				processTerminated = true;
			}
		}

		public void offer(Message lm) {
			if (processTerminated) return;
			itemsToDelete.offer(lm);
		}
	}

	private class RoleModifier extends Thread {
		private LinkedBlockingQueue<RoleModifyTask> itemsToDelete = new LinkedBlockingQueue<>();
		private volatile boolean processTerminated = false;

		RoleModifier() {
			start();
		}

		public void run() {
			try {
				while (!Launcher.killAllThreads) {
					final RoleModifyTask roleToModify = itemsToDelete.take();
					if (roleToModify != null) {
						if (roleToModify.isAdd()) {
							roleToModify.getRole().getGuild().getManager().addRoleToUser(roleToModify.getUser(), roleToModify.getRole());
						} else {
							roleToModify.getRole().getGuild().getManager().removeRoleFromUser(roleToModify.getUser(), roleToModify.getRole());
						}
					}
					Thread.sleep(1000L);
				}
			} catch (InterruptedException ignored) {
			} finally {
				processTerminated = true;
			}
		}

		public void offer(RoleModifyTask lm) {
			if (processTerminated) return;
			itemsToDelete.offer(lm);
		}
	}
}