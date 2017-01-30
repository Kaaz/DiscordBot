/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.handler;

import discordbot.db.controllers.CMusic;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OMusic;
import discordbot.db.model.OPlaylist;
import discordbot.guildsettings.music.SettingMusicSkipAdminOnly;
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

    public synchronized boolean handle(String messageId, TextChannel channel, User invoker, MessageReaction.ReactionEmote emote, boolean isAdding) {
        String guildId = channel.getGuild().getId();
        if (!isListening(guildId, messageId)) {
            return false;
        }
        MusicPlayerHandler player = MusicPlayerHandler.getFor(channel.getGuild(), discordBot);
        SimpleRank rank = discordBot.security.getSimpleRank(invoker, channel);
        if (!GuildSettings.get(channel.getGuild()).canUseMusicCommands(invoker, rank)) {
            return false;
        }
        if (!player.isPlaying()) {
            return false;
        }
        if (!player.isInVoiceWith(channel.getGuild(), invoker)) {
            return false;
        }
        if (Emojibet.NEXT_TRACK.equals(emote.getName())) {
            handleVoteSkip(player, channel, invoker, rank, isAdding);
            return true;
        }
        if (Emojibet.STAR.equals(emote.getName())) {
            OPlaylist playlist = CPlaylist.findBy(CUser.getCachedId(invoker.getId()), 0);
            if (playlist.id == 0) {
                playlist.setEditType(OPlaylist.EditType.PRIVATE_AUTO);
                CPlaylist.insert(playlist);
            }
            if (isAdding) {
                CPlaylist.addToPlayList(playlist.id, player.getCurrentlyPlaying());
            } else {
                CPlaylist.removeFromPlayList(playlist.id, player.getCurrentlyPlaying());
            }
            return true;
        } else if (Emojibet.NO_ENTRY.equals(emote.getName())) {
            handleBanTrack(player, channel, invoker, rank, isAdding);
            return true;
        }
        return false;
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
        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN) && "true".equals(GuildSettings.getFor(channel, SettingMusicSkipAdminOnly.class))) {
            return;
        }
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
