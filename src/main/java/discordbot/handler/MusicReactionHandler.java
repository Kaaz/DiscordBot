package discordbot.handler;

import discordbot.db.controllers.CMusic;
import discordbot.db.model.OMusic;
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
		String guildId = channel.getGuild().getId();
		if (!isListening(guildId, messageId)) {
			return;
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(channel.getGuild(), discordBot);
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

		if (Emojibet.NEXT_TRACK.equals(emote.getName())) {
			handleVoteSkip(player, channel, invoker, rank, isAdding);
		} else if (Emojibet.NO_ENTRY.equals(emote.getName())) {
			handleBanTrack(player, channel, invoker, rank, isAdding);
		}
	}

	private void handleBanTrack(MusicPlayerHandler player, TextChannel channel, User invoker, SimpleRank rank, boolean isAdding) {
		if (!isAdding || !rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
			return;
		}
		OMusic song = CMusic.findById(player.getCurrentlyPlaying());
		if (song.id > 0) {
			song.banned = 1;
			CMusic.update(song);
			player.forceSkip();
		}
	}

	private void handleVoteSkip(MusicPlayerHandler player, TextChannel channel, User invoker, SimpleRank rank, boolean isAdding) {
		if (isAdding) {
			player.voteSkip(invoker);
		} else {
			player.unregisterVoteSkip(invoker);
		}
		if (player.getVoteCount() >= player.getRequiredVotes()) {
			clearGuild(channel.getGuild().getId());
			player.forceSkip();
		}
	}
}
