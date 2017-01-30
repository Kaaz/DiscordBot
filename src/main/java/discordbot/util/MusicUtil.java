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

package discordbot.util;

import discordbot.db.model.OMusic;
import discordbot.guildsettings.music.SettingMusicQueueOnly;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.main.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.List;

public class MusicUtil {
    private final static String BLOCK_INACTIVE = "\u25AC";
    private final static String BLOCK_ACTIVE = "\uD83D\uDD18";
    private final static String SOUND_CHILL = "\uD83D\uDD09";
    private final static String SOUND_LOUD = "\uD83D\uDD0A";
    private final static float SOUND_TRESHHOLD = 0.4F;
    private final static int BLOCK_PARTS = 10;

    /**
     * Returns a fancy now playing message
     *
     * @param player the musicplayer
     * @param record the record playing
     * @param member
     * @return an embedded message
     */
    public static MessageEmbed nowPlayingMessage(MusicPlayerHandler player, OMusic record, Member member) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setThumbnail("https://i.ytimg.com/vi/" + record.youtubecode + "/0.jpg");
        embed.setTitle("\uD83C\uDFB6 " + record.youtubeTitle);
        embed.setDescription("[source](https://www.youtube.com/watch?v=" + record.youtubecode + ") | `" + DisUtil.getCommandPrefix(player.getGuild()) + "pl` - " + player.getPlaylist().title);
        embed.addField("duration", Misc.getDurationString(record.duration), true);
        String optionsField = "";
        if (player.getRequiredVotes() != 1) {
            optionsField += "Skips req.: " + player.getRequiredVotes() + Config.EOL;
        }
        String requiredRole = GuildSettings.get(player.getGuild()).getOrDefault(SettingMusicRole.class);
        if (!requiredRole.equals("false")) {
            optionsField += "Role req.: " + requiredRole + Config.EOL;
        }
        if (GuildSettings.get(player.getGuild()).getOrDefault(SettingMusicQueueOnly.class).equals("false")) {
            optionsField += "Random after queue";
        } else {
            optionsField += "Stop after queue";
        }
        embed.addField("Options:", optionsField, true);
        List<OMusic> queue = player.getQueue();
        int show = 3;
        if (!queue.isEmpty()) {
            String x = "";
            for (int i = 0; i < Math.min(show, queue.size()); i++) {
                x += queue.get(i).youtubeTitle + Config.EOL;
            }
            if (queue.size() > show) {
                x += ".. and **" + (queue.size() - 3) + "** more";
            }
            embed.addField("Next up", x, true);
        }
        if (member != null) {
            embed.setFooter("requested by " + member.getEffectiveName(), member.getUser().getAvatarUrl());
        } else {
            embed.setFooter("You add to your playlist and vote to skip with reactions", null);
        }
        return embed.build();
    }

    public static String nowPlayingMessageNoEmbed(MusicPlayerHandler player, OMusic record) {
        return "[`" + DisUtil.getCommandPrefix(player.getGuild()) + "pl` " + player.getPlaylist().title + "] \uD83C\uDFB6 " + record.youtubeTitle;
    }


    /**
     * @param startTime timestamp (in seconds) of the moment the song started playing
     * @param duration  current song length in seconds
     * @param volume    volume of the player
     * @return a formatted mediaplayer
     */
    public static String getMediaplayerProgressbar(long startTime, long duration, float volume, boolean isPaused) {
        long current = System.currentTimeMillis() / 1000 - startTime;
        String bar = isPaused ? "\u23EF" : "\u23F8 ";
        int activeBLock = (int) ((float) current / (float) duration * (float) BLOCK_PARTS);
        for (int i = 0; i < BLOCK_PARTS; i++) {
            if (i == activeBLock) {
                bar += BLOCK_ACTIVE;
            } else {
                bar += BLOCK_INACTIVE;
            }
        }
        bar += " [" + Misc.getDurationString(current) + "/" + Misc.getDurationString(duration) + "] ";
        if (volume >= SOUND_TRESHHOLD) {
            bar += SOUND_LOUD;
        } else {
            bar += SOUND_CHILL;
        }
        return bar;
    }
}
