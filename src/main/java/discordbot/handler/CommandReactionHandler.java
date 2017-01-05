package discordbot.handler;

import discordbot.command.CommandReactionListener;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2017-01-05.
 */
public class CommandReactionHandler {
	final ConcurrentHashMap<String, ConcurrentHashMap<String, CommandReactionListener<?>>> reactions;
	final DiscordBot bot;

	public CommandReactionHandler(DiscordBot bot) {
		this.bot = bot;
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

	public void handle(TextChannel channel, String messageId, MessageReaction reaction) {
		CommandReactionListener<?> listener = reactions.get(channel.getGuild().getId()).get(messageId);
		if (listener.hasReaction(reaction.getEmote().getName())) {
			channel.getMessageById(messageId).queue(message -> listener.react(reaction.getEmote().getName(), message));
		}

	}

	public boolean canHandle(String guildId, String messageId) {
		return reactions.containsKey(guildId) && reactions.get(guildId).containsKey(messageId);
	}
}
