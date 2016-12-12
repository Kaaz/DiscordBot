package discordbot.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.MessageChannel;

public class DebugUtil {
	/**
	 * Handles the debug output + response
	 *
	 * @param channel the channel to send the messages to
	 * @param output  the output to upload
	 */
	public static void handleDebug(MessageChannel channel, String output) {
		channel.sendMessage("One moment, uploading results: ").queue(message -> {
			String result = DebugUtil.sendToHastebin(output);
			if (result == null) {
				message.editMessage("Uploading failed!").queue();
			} else {
				message.editMessage("Here you go: " + result).queue();
			}
		});

	}

	/**
	 * attempts to send the message to hastebin
	 *
	 * @param message the message to send
	 * @return the url or null
	 */
	public static String sendToHastebin(String message) {
		try {
			return "http://hastebin.com/" + handleHastebin(message);
		} catch (UnirestException ignored) {
		}
		return null;
	}

	/**
	 * dumps a string to hastebin
	 *
	 * @param message the text to send
	 * @return key how to find it
	 */
	private static String handleHastebin(String message) throws UnirestException {
		return Unirest.post("http://hastebin.com/documents").body(message).asJson().getBody().getObject().getString("key");
	}
}
