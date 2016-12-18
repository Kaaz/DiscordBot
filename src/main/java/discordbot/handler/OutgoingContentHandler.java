package discordbot.handler;

import discordbot.handler.discord.RoleModifyTask;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OutgoingContentHandler {
	private final DiscordBot botInstance;
	private final RoleModifier roleThread;

	public OutgoingContentHandler(DiscordBot b) {
		botInstance = b;
		roleThread = new RoleModifier();
	}

	/**
	 * @param channel  channel to send to
	 * @param content  the message
	 * @param callback callback to execute after the message is sent
	 */
	public void sendAsyncMessage(MessageChannel channel, String content, Consumer<Message> callback) {
		channel.sendMessage(content.substring(0, Math.min(1999, content.length()))).queue(callback, throwable -> callback.accept(null));
	}

	public void sendAsyncMessage(MessageChannel channel, String content) {
		channel.sendMessage(content.substring(0, Math.min(1999, content.length()))).queue((message) -> {
			if (botInstance.shouldCleanUpMessages(channel)) {
				botInstance.schedule(() -> {
					if (message != null) {
						message.deleteMessage().queue();
					}
				}, Config.DELETE_MESSAGES_AFTER, TimeUnit.MILLISECONDS);
			}
		});
	}

	public void editAsync(Message message, String content, Consumer<Message> callback) {
		message.editMessage(content.substring(0, Math.min(1999, content.length()))).queue(callback);
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
		sendPrivateMessage(botInstance.client.getUserById(Config.CREATOR_ID), errorMessage);
	}

	/**
	 * send a message to creator {@link Config#CREATOR_ID}
	 * has to be in the {@link Config#BOT_GUILD_ID } bot's guild
	 *
	 * @param message the message to send
	 */
	public void sendMessageToCreator(String message) {
		User user = botInstance.client.getUserById(Config.CREATOR_ID);
		if (user != null) {
			sendPrivateMessage(user, message);
		} else {
			sendPrivateMessage(botInstance.getContainer().getBotFor(Config.BOT_GUILD_ID).client.getUserById(Config.CREATOR_ID), message);
		}
	}

	/**
	 * Sends a private message to user
	 *
	 * @param target  the user to send it to
	 * @param message the message
	 */
	public void sendPrivateMessage(User target, String message) {
		target.openPrivateChannel().queue(c -> c.sendMessage(message).queue());
	}


	private class RoleModifier extends Thread {
		private LinkedBlockingQueue<RoleModifyTask> itemsToDelete = new LinkedBlockingQueue<>();
		private volatile boolean processTerminated = false;

		RoleModifier() {
			start();
		}

		public void run() {
			try {
				while (!Launcher.isBeingKilled) {
					final RoleModifyTask roleToModify = itemsToDelete.take();
					if (roleToModify != null) {
						Guild guild = roleToModify.getRole().getGuild();
						Member member = guild.getMember(roleToModify.getUser());
						if (roleToModify.isAdd()) {
							guild.getController().addRolesToMember(member, roleToModify.getRole()).queue();
						} else {
							guild.getController().removeRolesFromMember(member, roleToModify.getRole()).queue();
						}
					}
					sleep(2_000L);
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