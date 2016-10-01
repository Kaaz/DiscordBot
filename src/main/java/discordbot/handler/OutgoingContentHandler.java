package discordbot.handler;

import discordbot.core.Logger;
import discordbot.handler.discord.RoleModifyTask;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.Misc;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.util.concurrent.LinkedBlockingQueue;

public class OutgoingContentHandler {
	private final DiscordBot bot;
	private final static long DELETE_INTERVAL = 500L;
	private final MessageDeleter deleteThread;
	private final RoleModifier roleThread;

	public OutgoingContentHandler(DiscordBot b) {
		bot = b;
		deleteThread = new MessageDeleter();
		roleThread = new RoleModifier();
	}

	/**
	 * @param channel channel to send to
	 * @param content the message
	 * @return IMessage or null
	 */
	public IMessage sendMessage(IChannel channel, String content) {
		RequestBuffer.RequestFuture<IMessage> request = bot.out.sendMessage(new MessageBuilder(bot.instance).withChannel(channel).withContent(content));
		return request.get();
	}

	/**
	 * adds a role to a user
	 *
	 * @param user the user
	 * @param role the role
	 */
	public void addRole(IUser user, IRole role) {
		roleThread.offer(new RoleModifyTask(user, role, true));
	}

	/**
	 * removes a role from a user
	 *
	 * @param user the user
	 * @param role the role
	 */
	public void removeRole(IUser user, IRole role) {
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
		sendPrivateMessage(bot.instance.getUserByID(Config.CREATOR_ID), errorMessage);
	}

	public void sendMessageToCreator(String message) {
		sendPrivateMessage(bot.instance.getUserByID(Config.CREATOR_ID), message);
	}

	/**
	 * Sends a private message to user
	 *
	 * @param target  the user to send it to
	 * @param message the message
	 */
	public void sendPrivateMessage(IUser target, String message) {
		RequestBuffer.request(() -> {
			try {
				IPrivateChannel pmChannel = bot.instance.getOrCreatePMChannel(target);
				return pmChannel.sendMessage(message);
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1500, "editMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}

	/**
	 * Edits an existing message
	 *
	 * @param msg     the message to edit
	 * @param newText new content of the message
	 * @return the message or null
	 */
	public RequestBuffer.RequestFuture<IMessage> editMessage(IMessage msg, String newText) {
		return RequestBuffer.request(() -> {
			try {
				return msg.edit(newText);
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1500, "editMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}

	/**
	 * Puts a message in the delete queue
	 *
	 * @param message the message to delete
	 */
	public void deleteMessage(IMessage message) {
		deleteThread.offer(message);
	}

	public RequestBuffer.RequestFuture<IMessage> sendMessage(MessageBuilder builder) {
		return RequestBuffer.request(() -> {
			try {
				return builder.send();
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1000, "sendMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			} catch (RateLimitException e) {
				System.out.println(e.getRetryDelay());
				System.out.println(e.getMethod());
				throw e;
			}
			return null;
		});
	}

	/**
	 * simple thread to delete messages, since it bugs out otherwise
	 */
	private class MessageDeleter extends Thread {
		private LinkedBlockingQueue<IMessage> itemsToDelete = new LinkedBlockingQueue<>();
		private volatile boolean processTerminated = false;

		MessageDeleter() {
			start();
		}

		public void run() {
			try {
				while (!Launcher.killAllThreads) {
					final IMessage msgToDelete = itemsToDelete.take();
					if (msgToDelete != null) {
						RequestBuffer.request(() -> {
							try {
								msgToDelete.delete();
								return true;
							} catch (MissingPermissionsException | DiscordException e) {
								System.out.println(e.getMessage());
								e.printStackTrace();
							}
							return false;
						});
					} else {
						System.out.println("MSG IS NULL");
					}
					Thread.sleep(DELETE_INTERVAL);
				}
			} catch (InterruptedException ignored) {
			} finally {
				processTerminated = true;
			}
		}

		public void offer(IMessage lm) {
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
						RequestBuffer.request(() -> {
							try {
								if (roleToModify.isAdd()) {
									roleToModify.getUser().addRole(roleToModify.getRole());
								} else {
									roleToModify.getUser().removeRole(roleToModify.getRole());
								}
								return true;
							} catch (MissingPermissionsException | DiscordException e) {
								if (!e.getMessage().startsWith("Edited roles hierarchy is too high")) {
									System.out.println(e.getMessage());
									bot.out.sendErrorToMe(e, "server", roleToModify.getRole().getGuild().getName(), "user", roleToModify.getRole().getName(), "Modifier", roleToModify.isAdd() ? "Adding" : "Removing");
									e.printStackTrace();
								}
							}
							return false;
						});
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