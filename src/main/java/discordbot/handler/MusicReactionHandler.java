package discordbot.handler;

import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicReactionHandler {

	private final Map<String, HashSet<String>> listeningMessages;
	private final DiscordBot discordBot;

	public MusicReactionHandler(DiscordBot discordBot) {
		this.discordBot = discordBot;
		listeningMessages = new ConcurrentHashMap<>();
	}

	public synchronized void addMessage(String guildId, String id) {
		if (!listeningMessages.containsKey(guildId)) {
			listeningMessages.put(guildId, new HashSet<>());
		}
		listeningMessages.get(guildId).add(id);
	}

	public synchronized boolean isListening(String guildId, String messageId) {
		return listeningMessages.containsKey(guildId) && listeningMessages.get(guildId).contains(messageId);
	}

	public synchronized void removeMessage(String guildId, String id) {
		if (listeningMessages.containsKey(guildId))
			listeningMessages.get(guildId).remove(id);
	}

	public synchronized void clearGuild(String guildId) {
		if (listeningMessages.containsKey(guildId)) {
			listeningMessages.get(guildId).clear();
		}
	}

	public synchronized void handle(String messageId, TextChannel channel, User invoker, MessageReaction.ReactionEmote emote, boolean isAdding) {
		System.out.println("HANDLING");
		String guildId = channel.getGuild().getId();
		if (!isListening(guildId, messageId)) {
			return;
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(channel.getGuild(), discordBot);
		if (!Emojibet.NEXT_TRACK.equals(emote.getName())) {
			return;
		}
		SimpleRank rank = discordBot.security.getSimpleRank(invoker, channel);
		if (!GuildSettings.get(channel.getGuild()).canUseMusicCommands(invoker, rank)) {
			return;
		}
		if (!player.isPlaying()) {
			return;
		}
		if (!player.isInVoiceWith(channel.getGuild(), invoker)) {
			return;
		}
		if (isAdding) {
			player.voteSkip(invoker);
		} else {
			player.unregisterVoteSkip(invoker);
		}
		if (player.getVoteCount() >= player.getRequiredVotes()) {
			clearGuild(guildId);
			player.forceSkip();
		}
	}
}
