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

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.vdurmont.emoji.EmojiParser;
import emily.command.CommandVisibility;
import emily.command.ICommandCleanup;
import emily.core.AbstractCommand;
import emily.db.controllers.CMusic;
import emily.db.controllers.CPlaylist;
import emily.db.controllers.CUser;
import emily.db.model.OMusic;
import emily.db.model.OPlaylist;
import emily.db.model.OUser;
import emily.guildsettings.music.SettingMusicRole;
import emily.handler.GuildSettings;
import emily.handler.MusicPlayerHandler;
import emily.handler.Template;
import emily.main.Config;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.YTSearch;
import emily.util.YTUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * !play
 * plays a youtube link
 * yea.. play is probably not a good name at the moment
 */
public class PlayCommand extends AbstractCommand implements ICommandCleanup {
    private YTSearch ytSearch;

    public PlayCommand() {
        super();
        ytSearch = new YTSearch();
    }

    @Override
    public void cleanup() {
        ytSearch.resetCache();
        if (!ytSearch.hasValidKey()) {
            for (String key : Config.GOOGLE_API_KEY) {
                ytSearch.addYoutubeKey(key);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Plays a song from youtube";
    }

    @Override
    public String getCommand() {
        return "play";
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "play <youtubelink>    //download and plays song",
                "play <part of title>  //shows search results",
                "play                  //just start playing something"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{"p"};
    }

    private boolean isInVoiceWith(Guild guild, User author) {
        VoiceChannel channel = guild.getMember(author).getVoiceState().getChannel();
        if (channel == null) {
            return false;
        }
        for (Member user : channel.getMembers()) {
            if (user.getUser().getId().equals(guild.getJDA().getSelfUser().getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        TextChannel txt = (TextChannel) channel;
        Guild guild = txt.getGuild();
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
            return Template.get(channel, "music_required_role_not_found", guild.getRoleById(GuildSettings.getFor(channel, SettingMusicRole.class)).getName());
        }

        if (!PermissionUtil.checkPermission(txt, guild.getSelfMember(), Permission.MESSAGE_WRITE)) {
            return "";
        }
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        if (!isInVoiceWith(guild, author)) {
            VoiceChannel vc = guild.getMember(author).getVoiceState().getChannel();
            if (vc == null) {
                return "you are not in a voicechannel";
            }
            try {
                if (player.isConnected()) {
                    if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
                        return Template.get("music_not_same_voicechannel");
                    }
                    player.leave();
                }
                if (!PermissionUtil.checkPermission(vc, guild.getSelfMember(), Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
                    return Template.get("music_join_no_permission", vc.getName());
                }
                if (!PermissionUtil.checkPermission(vc, guild.getSelfMember(), Permission.MANAGE_CHANNEL)
                        && vc.getUserLimit() != 0 && vc.getUserLimit() <= vc.getMembers().size()) {
                    return Template.get("music_join_channel_full", vc.getName());
                }
                player.connectTo(vc);
            } catch (Exception e) {
                e.printStackTrace();
                return "Can't connect to you";
            }
        } else if (MusicPlayerHandler.getFor(guild, bot).getUsersInVoiceChannel().size() == 0) {
            return Template.get("music_no_users_in_channel");
        }
        if (args.length > 0) {
            final String videoTitle;
            String videoCode = YTUtil.isValidYoutubeCode(args[0]) ? args[0] : YTUtil.extractCodeFromUrl(args[0]);
            String playlistCode = YTUtil.getPlayListCode(args[0]);
            if (playlistCode != null) {
                if (!ytSearch.hasValidKey()) {
                    return Template.get("music_no_valid_youtube_key", YTUtil.nextApiResetTime());
                }
                if (userRank.isAtLeast(SimpleRank.CONTRIBUTOR) || CUser.findBy(author.getId()).hasPermission(OUser.PermissionNode.IMPORT_PLAYLIST)) {
                    List<YTSearch.SimpleResult> items = ytSearch.getPlayListItems(playlistCode);
                    int playCount = 0;
                    for (YTSearch.SimpleResult track : items) {
                        processTrack(player, bot, (TextChannel) channel, author, track.getCode(), track.getTitle(), false);
                        if (++playCount == Config.MUSIC_MAX_PLAYLIST_SIZE) {
                            break;
                        }
                    }
                    return String.format("Added **%s** items to the add", playCount);
                }
            }
            if (!YTUtil.isValidYoutubeCode(videoCode)) {
                if (!ytSearch.hasValidKey()) {
                    return Template.get("music_no_valid_youtube_key", YTUtil.nextApiResetTime());
                }
                YTSearch.SimpleResult results = ytSearch.getResults(Joiner.on(" ").join(args));
                if (results != null) {
                    videoCode = results.getCode();
                    videoTitle = EmojiParser.parseToAliases(results.getTitle());
                } else {
                    videoCode = null;
                    videoTitle = "";
                }
            } else {
                videoTitle = videoCode;
            }
            if (videoCode != null && YTUtil.isValidYoutubeCode(videoCode)) {
                return processTrack(player, bot, (TextChannel) channel, author, videoCode, videoTitle, true);
            } else {
                return Template.get("command_play_no_results");
            }
        } else {
            if (player.isPlaying()) {
                if (player.isPaused()) {
                    player.togglePause();
                }
                return "";
            }
            if (player.playRandomSong()) {
                return Template.get("music_started_playing_random");
            } else {
                OPlaylist pl = CPlaylist.findById(player.getActivePLaylistId());
                if (!pl.isGlobalList()) {
                    if (CPlaylist.getMusicCount(pl.id) == 0) {
                        return Template.get("music_failed_playlist_empty", pl.title);
                    }
                }
                return Template.get("music_failed_to_start");
            }
        }
    }

    public static String processTrack(MusicPlayerHandler player, DiscordBot bot, TextChannel channel, User invoker, String videoCode, String videoTitle, boolean useTemplates) {
        OMusic record = CMusic.findByYoutubeId(videoCode);
        final File filecheck;
        if (record.id > 0 && record.fileExists == 1) {
            filecheck = new File(record.filename);
        } else {
            filecheck = new File(YTUtil.getOutputPath(videoCode));
        }
        final String finalVideoCode = videoCode;
        Consumer<Message> consumer = message -> bot.getContainer().downloadRequest(finalVideoCode, videoTitle, message, msg -> {
            try {
                File targetFile = new File(YTUtil.getOutputPath(videoCode));
                if (targetFile.exists()) {
                    if (msg != null) {
                        bot.out.editBlocking(msg, ":notes: Found *" + videoTitle + "* And added it to the add");
                    }
                    player.addToQueue(targetFile.toPath().toRealPath().toString(), invoker);
                } else {
                    if (player.getPlaylist().isGlobalList()) {
                        if (msg != null) {
                            bot.out.editBlocking(msg, "Download failed, the song is likely too long or region locked!");
                        }
                    } else {
                        CPlaylist.removeFromPlayList(player.getPlaylist().id, record.id);
                        if (msg != null) {
                            bot.out.editBlocking(msg, String.format("the video `%s` (%s) is unavailable and its removed from the playlist '%s'",
                                    finalVideoCode, record.youtubeTitle, player.getPlaylist().title));
                        }
                        player.forceSkip();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (msg != null) {
                    bot.out.editBlocking(msg, (Template.get("music_file_error")));
                }
            }
        });
        boolean isInProgress = bot.getContainer().isInProgress(videoCode);
        if (!filecheck.exists() && !isInProgress) {
            if (useTemplates) {
                Message message = bot.out.sendBlock(channel, Template.get("music_downloading_in_queue", videoTitle));
                consumer.accept(message);
            } else {
                consumer.accept(null);
            }
            return "";
        } else if (YTUtil.isValidYoutubeCode(videoCode) && isInProgress) {
            return Template.get(channel, "music_downloading_in_progress", videoTitle);
        }
        try {
            String path = filecheck.toPath().toRealPath().toString();
            OMusic rec = CMusic.findByFileName(path);
            CMusic.registerPlayRequest(rec.id);
            player.addToQueue(path, invoker);
            if (useTemplates) {
                return Template.get("music_added_to_queue", rec.youtubeTitle);
            }
            return "\u25AA " + rec.youtubeTitle;
        } catch (Exception e) {
            bot.getContainer().reportError(e, "ytcode", videoCode);
            return Template.get("music_file_error");
        }
    }
}