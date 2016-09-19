package novaz.handler;

import novaz.core.Logger;
import novaz.main.Config;
import novaz.main.Launcher;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.*;

import java.util.concurrent.LinkedBlockingQueue;

public class OutgoingContentHandler {
	private final NovaBot bot;
	private final static long DELETE_INTERVAL = 500L;
	private final MessageDeleter deleteThread;

	public OutgoingContentHandler(NovaBot b) {
		bot = b;
		deleteThread = new MessageDeleter();
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
}
