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

package emily.command.music;

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.db.controllers.CMusic;
import emily.db.controllers.CMusicVote;
import emily.db.controllers.CPlaylist;
import emily.db.controllers.CUser;
import emily.db.model.OMusic;
import emily.db.model.OMusicVote;
import emily.db.model.OPlaylist;
import emily.db.model.OUser;
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.handler.MusicPlayerHandler;
import emily.handler.Template;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import emily.util.MusicUtil;
import emily.util.TimeUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * !current
 * retrieves information about the currently playing track
 */
public class NowPlayingCommand extends AbstractCommand {
    private static final Pattern votePattern = Pattern.compile("^(?>vote|rate)\\s?(\\d+)?$");


    public NowPlayingCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "retrieves information about the song currently playing";
    }

    @Override
    public String getCommand() {
        return "current";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "current                 //info about the currently playing song",
                "current seek <time>     //go to specified timestamp of track (eg. 3m10s)",
                "current vote <1-10>     //Cast your vote to the song; 1=worst, 10=best",
                "current repeat          //repeats the currently playing song",
                "current update          //updates the now playing message every 10 seconds",
                "current updatetitle     //updates the topic of the music channel every 10 seconds",
                "current source          //Shows the source of the video",
                "current pm              //sends you a private message with the details",
                "",
                "current clear               //clears everything in the queue",
                "current clear admin         //check if clear is admin-only",
                "current clear admin toggle  //switch between admin-only and normal",
//				"current artist        //sets the artist of current song",
//				"current correct       //accept the systems suggestion of title/artist",
//				"current reversed      //accept the systems suggestion in reverse [title=artist,artist=title]",
//				"current title <title> //sets title of current song",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{"playing", "np", "nowplaying"};
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        Guild guild = ((TextChannel) channel).getGuild();
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
            return Template.get(channel, "music_required_role_not_found", guild.getRoleById(GuildSettings.getFor(channel, GSetting.MUSIC_ROLE_REQUIREMENT)).getName());
        }
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        OMusic song = CMusic.findById(player.getCurrentlyPlaying());
        if (song.id == 0 && (args.length == 0 || !args[0].equals("clear"))) {
            return Template.get("command_currentlyplaying_nosong");
        }
        if (args.length == 0 && PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
            bot.queue.add(channel.sendMessage(MusicUtil.nowPlayingMessage(player, song, null)));
            return "";
        }
        //
        if (args.length > 0) {
            String voteInput = args[0].toLowerCase();
            if (args.length > 1) {
                voteInput += " " + args[1];
            }
            Matcher m = votePattern.matcher(voteInput);
            if (m.find()) {
                OMusicVote voteRecord = CMusicVote.findBy(song.id, author.getId());
                if (m.group(1) != null) {
                    int vote = Math.max(1, Math.min(10, Misc.parseInt(m.group(1), 0)));
                    CMusicVote.insertOrUpdate(song.id, author.getId(), vote);
                    return "vote is registered (" + vote + ")";
                }
                if (voteRecord.vote > 0) {
                    return Template.get("music_your_vote", song.youtubeTitle, voteRecord.vote);
                } else {
                    return Template.get("music_not_voted", DisUtil.getCommandPrefix(channel) + "np vote ");
                }
            }
        }

        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "seek":
                case "goto":
                    player.goToTime(TimeUtil.toMillis(Misc.joinStrings(args, 1)));
                    return "";
                case "repeat":
                    boolean repeatMode = !player.isInRepeatMode();
                    player.setRepeat(repeatMode);
                    if (repeatMode) {
                        return Template.get("music_repeat_mode");
                    }
                    return Template.get("music_repeat_mode_stopped");
                case "ban":
                    if (userRank.isAtLeast(SimpleRank.CONTRIBUTOR) || CUser.findBy(author.getId()).hasPermission(OUser.PermissionNode.BAN_TRACKS)) {
                        song.banned = 1;
                        CMusic.update(song);
                        player.forceSkip();
                        return Template.get("command_current_banned_success");
                    }
                    return Template.get("no_permission");
                case "source":
                    return Template.get(channel, "music_source_location", "<https://www.youtube.com/watch?v=" + song.youtubecode + ">");
                case "pm":
                    bot.out.sendPrivateMessage(author,
                            "The track I'm playing now is: " + song.youtubeTitle + BotConfig.EOL +
                                    "You can find it here: https://www.youtube.com/watch?v=" + song.youtubecode
                    );
                    return Template.get(channel, "private_message_sent", guild.getMember(author).getEffectiveName());
                case "clear":
                    boolean adminOnly = "true".equals(GuildSettings.getFor(channel, GSetting.MUSIC_CLEAR_ADMIN_ONLY));
                    if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN) && args.length > 2 && args[1].equals("admin") && args[2].equalsIgnoreCase("toggle")) {
                        GuildSettings.get(guild).set(guild, GSetting.MUSIC_SKIP_ADMIN_ONLY, adminOnly ? "false" : "true");
                        adminOnly = !adminOnly;
                    } else if ((userRank.isAtLeast(SimpleRank.GUILD_ADMIN) || !adminOnly) && args.length == 1) {
                        player.clearQueue();
                        return Template.get("music_queue_cleared");
                    }
                    return Template.get("music_clear_mode", adminOnly ? "admin-only" : "normal");
            }
        }

        OPlaylist playlist = CPlaylist.findById(player.getActivePLaylistId());
        String ret = "";
        if (player.getRequiredVotes() > 1) {
            ret += player.getVoteCount() + "/" + player.getRequiredVotes() + Emojibet.NEXT_TRACK;
        }
        ret += "[`" + DisUtil.getCommandPrefix(channel) + "pl` " + playlist.title + "] " + "\uD83C\uDFB6 ";
        ret += song.youtubeTitle;
        final String autoUpdateText = ret;
        ret += BotConfig.EOL + BotConfig.EOL;
        MusicPlayerHandler musicHandler = MusicPlayerHandler.getFor(guild, bot);
        ret += MusicUtil.getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume(), musicHandler.isPaused()) + BotConfig.EOL + BotConfig.EOL;

        if (GuildSettings.get(guild).getOrDefault(GSetting.MUSIC_SHOW_LISTENERS).equals("true")) {
            List<Member> userList = musicHandler.getUsersInVoiceChannel();
            if (userList.size() > 0) {
                ret += "\uD83C\uDFA7  Listeners" + BotConfig.EOL;
                ArrayList<String> displayList = userList.stream().map(Member::getEffectiveName).collect(Collectors.toCollection(ArrayList::new));
                ret += Misc.makeTable(displayList);
            }
        }
        List<OMusic> queue = musicHandler.getQueue();
        if (queue.size() > 0) {
            ret += BotConfig.EOL + "\uD83C\uDFB5 *Next up:* " + BotConfig.EOL;
            for (int i = 0; i < Math.min(2, queue.size()); i++) {
                ret += "\uD83D\uDC49 " + queue.get(i).youtubeTitle + BotConfig.EOL;
            }
            if (queue.size() > 2) {
                ret += BotConfig.EOL + "... And **" + (queue.size() - 2) + "** more!";
            }

        }
        if (args.length == 1 && args[0].equals("update")) {
            final Future<?>[] f = {null};
            bot.queue.add(channel.sendMessage(ret),
                    message -> {
                        if (message == null) {
                            return;
                        }
                        bot.scheduleRepeat(
                                () -> {
                                    if (player.getCurrentlyPlaying() != song.id) {
                                        f[0].cancel(false);
                                        return;
                                    }
                                    bot.queue.add(message.editMessage((player.isInRepeatMode() ? "\uD83D\uDD02 " : "") + autoUpdateText + BotConfig.EOL +
                                            MusicUtil.getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume(), musicHandler.isPaused()) + BotConfig.EOL + BotConfig.EOL
                                    ));
                                }, 10_000L, 10_000L
                        );
                    });
            return "";
        } else if (args.length >= 1 && args[0].equals("updatetitle")) {
            if (!userRank.isAtLeast(SimpleRank.USER)) {
                return Template.get(channel, "command_no_permission");
            }
            if (player.isUpdateChannelTitle()) {
                player.setUpdateChannelTitle(false);
                return Template.get("music_channel_autotitle_stop");
            } else {
                TextChannel musicChannel = (TextChannel) channel;
                if (PermissionUtil.checkPermission(musicChannel, guild.getSelfMember(), Permission.MANAGE_CHANNEL)) {
                    player.setUpdateChannelTitle(true);
                    final Future<?>[] f = {null};
                    bot.scheduleRepeat(() -> {
                        if (!player.isUpdateChannelTitle() || !player.canTogglePause()) {
                            player.setUpdateChannelTitle(false);
                            bot.queue.add(musicChannel.getManager().setTopic(""));
                            f[0].cancel(false);
                            return;
                        }
                        OMusic nowPlaying = CMusic.findById(player.getCurrentlyPlaying());
                        bot.queue.add(musicChannel.getManager().setTopic(
                                (player.isInRepeatMode() ? "\uD83D\uDD02 " : "") +
                                        MusicUtil.getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume(), musicHandler.isPaused()) +
                                        (nowPlaying.id > 0 ? "\uD83C\uDFB6 " + nowPlaying.youtubeTitle : "")
                        ));
                    }, 10_000L, 10_000L);
                    return Template.get("music_channel_autotitle_start");
                }
                return Template.get("permission_missing_manage_channel");
            }
        }
        return (player.isInRepeatMode() ? "\uD83D\uDD01 " : "") + ret;
    }
}