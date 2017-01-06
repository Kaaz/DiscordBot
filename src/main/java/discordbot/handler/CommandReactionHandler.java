package discordbot.handler;

import discordbot.command.CommandReactionListener;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandReactionHandler {
	private final ConcurrentHashMap<String, ConcurrentHashMap<String, CommandReactionListener<?>>> reactions;

	public CommandReactionHandler() {
		reactions = new ConcurrentHashMap<>();
	}

	public void addReactionListener(String guildId, Message message, CommandReactionListener<?> handler) {
		if (!reactions.containsKey(guildId)) {
			reactions.put(guildId, new ConcurrentHashMap<>());
		}
		if (!reactions.get(guildId).containsKey(message.getId())) {
			for (String emote : handler.getEmotes()) {
				message.addReaction(emote).queue();
			}
			reactions.get(guildId).put(message.getId(), handler);
		}
	}

	/**
	 * Handles the reaction
	 *
	 * @param channel   TextChannel of the message
	 * @param messageId id of the message
	 * @param userId    id of the user reacting
	 * @param reaction  the reaction
	 */
	public void handle(TextChannel channel, String messageId, String userId, MessageReaction reaction) {
		CommandReactionListener<?> listener = reactions.get(channel.getGuild().getId()).get(messageId);
		if (listener.getExpireTimestamp() < System.currentTimeMillis()) {
			reactions.get(channel.getGuild().getId()).remove(messageId);
		} else if (listener.hasReaction(reaction.getEmote().getName()) && listener.getUserId().equals(userId)) {
			channel.getMessageById(messageId).queue(message -> listener.react(reaction.getEmote().getName(), message));
		}

	}

	/**
	 * Do we have an event for a message?
	 *
	 * @param guildId   discord guild-id of the message
	 * @param messageId id of the message
	 * @return do we have an handler?
	 */
	public boolean canHandle(String guildId, String messageId) {
		return reactions.containsKey(guildId) && reactions.get(guildId).containsKey(messageId);
	}

	public synchronized void removeGuild(String guildId) {
		reactions.remove(guildId);
	}

	/**
	 * Delete expired handlers
	 */
	public synchronized void cleanCache() {
		long now = System.currentTimeMillis();
		Iterator<ConcurrentHashMap<String, CommandReactionListener<?>>> mi = reactions.values().iterator();
		for (Iterator<Map.Entry<String, ConcurrentHashMap<String, CommandReactionListener<?>>>> iterator = reactions.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<String, ConcurrentHashMap<String, CommandReactionListener<?>>> mapEntry = iterator.next();
			mapEntry.getValue().values().removeIf(listener -> listener.getExpireTimestamp() < now);
			if (mapEntry.getValue().values().isEmpty()) {
				reactions.remove(mapEntry.getKey());
			}
		}
	}
}
