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
import emily.command.CommandReactionListener;
import emily.command.CommandVisibility;
import emily.command.ICommandReactionListener;
import emily.command.PaginationInfo;
import emily.core.AbstractCommand;
import emily.db.controllers.CGuild;
import emily.db.controllers.CMusic;
import emily.db.controllers.CPlaylist;
import emily.db.controllers.CUser;
import emily.db.model.OMusic;
import emily.db.model.OPlaylist;
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
import emily.util.YTUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * !playlist
 * shows the current songs in the add
 */
public class PlaylistCommand extends AbstractCommand implements ICommandReactionListener<PaginationInfo<OPlaylist>> {
    private final static int ITEMS_PER_PAGE = 20;

    public PlaylistCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "information about the playlists";
    }

    @Override
    public String getCommand() {
        return "playlist";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "-- using playlists ",
                "playlist mine           //use your default playlist",
                "playlist mine <code>    //use your playlist with code",
                "playlist guild          //use the guild's default playlist",
                "playlist guild <code>   //use the guild's playlist with code",
                "playlist global         //use the global playlist",
                "playlist settings       //check the settings for the active playlist",
                "playlist                //info about the current playlist",
                "playlist list <page>    //Shows the music in the playlist",
                "",
                "-- Adding and removing music from the playlist",
//				"playlist show <pagenumber>           //shows music in the playlist",
                "playlist add                         //adds the currently playing music",
                "playlist add guild                   //adds the currently playing to the guild list",
//				"playlist add <youtubelink>           //adds the link to the playlist",
                "playlist remove                      //removes the currently playing music",
//				"playlist remove <youtubelink>        //removes song from playlist",
                "playlist removeall                   //removes ALL songs from playlist",
                "",
                "-- Changing the settings of the playlist",
                "playlist title <new title>           //edit the playlist title",
                "playlist edit <new type>             //change the edit-type of a playlist",
                "playlist play <id>                   //plays a track from the playlist",
                "playlist playtype <new type>         //change the play-type of a playlist",
//				"playlist visibility <new visibility> //change who can see the playlist",
//				"playlist reset                       //reset settings to default",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "pl"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        Guild guild = ((TextChannel) channel).getGuild();
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        int nowPlayingId = player.getCurrentlyPlaying();
        OMusic musicRec = CMusic.findById(nowPlayingId);
        if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
            return Template.get(channel, "music_required_role_not_found", guild.getRoleById(GuildSettings.getFor(channel, GSetting.MUSIC_ROLE_REQUIREMENT)).getName());
        }
        OPlaylist playlist = CPlaylist.findById(player.getActivePLaylistId());
        if (playlist.id == 0) {
            playlist = CPlaylist.getGlobalList();
        }
        String cp = DisUtil.getCommandPrefix(channel);
        if (args.length == 0) {
            if (playlist.isGlobalList()) {
                return Template.get(channel, "music_playlist_using", playlist.title) + " See `" + cp + "pl help` for more info" + BotConfig.EOL +
                        "You can switch to a different playlist with `" + cp + "pl guild` to the guild's list or `" + cp + "pl mine` to your own one";
            }
            return Template.get(channel, "music_playlist_using", playlist.title) +
                    "Settings " + makeSettingsTable(playlist) +
                    "To add the currently playing music to the playlist use `" + DisUtil.getCommandPrefix(channel) + "pl add`, check out `" + DisUtil.getCommandPrefix(channel) + "help pl` for more info";
        }
        OPlaylist newlist = null;

        switch (args[0].toLowerCase()) {
            case "mine":
            case "guild":
            case "global":
                String playlistCode = null;
                if (args.length > 1) {
                    playlistCode = Misc.joinStrings(args, 1);
                    if (playlistCode.length() > 32) {
                        playlistCode = playlistCode.substring(0, 32);
                    }
                }
                if (playlistCode == null || playlistCode.isEmpty()) {
                    playlistCode = "default";
                }
                newlist = findPlaylist(args[0], playlistCode, author, guild);
                break;
        }
        if (newlist != null) {
            player.setActivePlayListId(newlist.id);
            return Template.get(channel, "music_playlist_changed", newlist.title);
        }

        switch (args[0].toLowerCase()) {
            case "add":
            case "+":
                if (nowPlayingId == 0) {
                    return Template.get(channel, "command_currentlyplaying_nosong");
                }
                if (canAddTracks(playlist, (TextChannel) channel, author, userRank)) {
                    if (CPlaylist.isInPlaylist(playlist.id, nowPlayingId)) {
                        return Template.get(channel, "playlist_music_already_added", musicRec.youtubeTitle, playlist.title);
                    }
                    CPlaylist.addToPlayList(playlist.id, nowPlayingId);
                    return Template.get(channel, "playlist_music_added", musicRec.youtubeTitle, playlist.title);
                }
                return Template.get(channel, "no_permission");
            case "removeall":
                if (isPlaylistAdmin(playlist, (TextChannel) channel, author, userRank)) {
                    CPlaylist.resetPlaylist(playlist.id);
                    return Template.get(channel, "playlist_music_removed_all", playlist.title);
                }
                return Template.get(channel, "no_permission");
            case "remove":
            case "del":
            case "-":
                if (args.length > 1 && (args[1].equals("guild") || args[1].equals("g"))) {
                    playlist = CPlaylist.findBy(0, CGuild.getCachedId(guild.getId()));
                } else if (args.length > 1 && args[1].matches("^\\d+$")) {
                    musicRec = CMusic.findById(Integer.parseInt(args[1]));
                    nowPlayingId = musicRec.id;
                } else if (args.length > 1 && YTUtil.isValidYoutubeCode(args[1])) {
                    musicRec = CMusic.findByYoutubeId(args[1]);
                    nowPlayingId = musicRec.id;
                }
                if (!canRemoveTracks(playlist, (TextChannel) channel, author, userRank)) {
                    return Template.get(channel, "no_permission");
                }
                CPlaylist.removeFromPlayList(playlist.id, nowPlayingId);
                return Template.get(channel, "playlist_music_removed", musicRec.youtubeTitle, playlist.title);
            case "list":
            case "music":
                if (playlist.isGlobalList()) {
                    return Template.get(channel, "playlist_global_readonly");
                }
                final int currentPage = 1;
                int totalTracks = CPlaylist.getMusicCount(playlist.id);
                int maxPage = (int) Math.ceil((double) totalTracks / (double) ITEMS_PER_PAGE);
                OPlaylist finalPlaylist = playlist;
                bot.queue.add(channel.sendMessage(makePage(guild, playlist, currentPage, maxPage)),
                        msg -> {
                            if (maxPage > 1) {
                                bot.commandReactionHandler.addReactionListener(((TextChannel) channel).getGuild().getId(), msg,
                                        getReactionListener(author.getId(), new PaginationInfo<>(currentPage, maxPage, guild, finalPlaylist)));
                            }
                        });
                return "";

            default:
                break;
        }

        if (args.length < 1 || args[0].equals("settings")) {
            return makeSettingsTable(playlist);
        }
        if (playlist.isGlobalList()) {
            return Template.get(channel, "playlist_global_readonly");
        }
        boolean isPlaylistAdmin = isPlaylistAdmin(playlist, (TextChannel) channel, author, userRank);
        switch (args[0].toLowerCase()) {
            case "title":
                if (args.length == 1 || !isPlaylistAdmin) {
                    return Template.get(channel, "command_playlist_title", playlist.title);
                }
                playlist.title = EmojiParser.parseToAliases(Joiner.on(" ").join(Arrays.copyOfRange(args, 1, args.length)));
                CPlaylist.update(playlist);
                player.setActivePlayListId(playlist.id);
                return Template.get(channel, "playlist_title_updated", playlist.title);
            case "edit-type":
            case "edittype":
            case "edit":
                if (args.length == 1 || !isPlaylistAdmin) {
                    List<List<String>> tbl = new ArrayList<>();
                    for (OPlaylist.EditType editType : OPlaylist.EditType.values()) {
                        if (editType.getId() < 1) continue;
                        tbl.add(Arrays.asList((editType == playlist.getEditType() ? "*" : " ") + editType.getId(), editType.toString(), editType.getDescription()));
                    }
                    return "the edit-type of the playlist. A `*` indicates the selected option" + BotConfig.EOL +
                            Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + BotConfig.EOL +
                            "To change the type use the \\#, for instance `" + DisUtil.getCommandPrefix(channel) + "pl edit 3` sets it to PUBLIC_ADD " + BotConfig.EOL + BotConfig.EOL +
                            "Private in a guild context refers to users with admin privileges";
                }
                if (args.length > 1 && args[1].matches("^\\d+$")) {
                    OPlaylist.EditType editType = OPlaylist.EditType.fromId(Integer.parseInt(args[1]));
                    if (editType.equals(OPlaylist.EditType.UNKNOWN)) {
                        Template.get(channel, "playlist_setting_invalid", args[1], "edittype");
                    }
                    playlist.setEditType(editType);
                    CPlaylist.update(playlist);
                    player.setActivePlayListId(playlist.id);
                    return Template.get(channel, "playlist_setting_updated", "edittype", args[1]);
                }
                return Template.get(channel, "playlist_setting_not_numeric", "edittype");
            case "vis":
            case "visibility":
                if (args.length == 1 || !isPlaylistAdmin) {
                    List<List<String>> tbl = new ArrayList<>();
                    for (OPlaylist.Visibility visibility : OPlaylist.Visibility.values()) {
                        if (visibility.getId() < 1) continue;
                        tbl.add(Arrays.asList((visibility == playlist.getVisibility() ? "*" : " ") + visibility.getId(), visibility.toString(), visibility.getDescription()));
                    }
                    return "the visibility-type of the playlist. A `*` indicates the selected option" + BotConfig.EOL +
                            Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + BotConfig.EOL +
                            "To change the type use the \\#, for instance `" + DisUtil.getCommandPrefix(channel) + "pl vis 3` sets it to guild " + BotConfig.EOL + BotConfig.EOL +
                            "Private in a guild-setting refers to users with admin privileges, use the number in the first column to set it";
                }
                if (args.length > 1 && args[1].matches("^\\d+$")) {
                    OPlaylist.Visibility visibility = OPlaylist.Visibility.fromId(Integer.parseInt(args[1]));
                    if (visibility.equals(OPlaylist.Visibility.UNKNOWN)) {
                        Template.get(channel, "playlist_setting_invalid", args[1], "visibility");
                    }
                    playlist.setVisibility(visibility);
                    CPlaylist.update(playlist);
                    player.setActivePlayListId(playlist.id);
                    return Template.get(channel, "playlist_setting_updated", "visibility", args[1]);
                }
                return Template.get("playlist_setting_not_numeric", "visibility");
            case "play":
                if (args.length > 1) {
                    OMusic record = null;
                    if (args[1].matches("^\\d+$")) {
                        record = CMusic.findById(Integer.parseInt(args[1]));
                    } else if (YTUtil.isValidYoutubeCode(args[1])) {
                        record = CMusic.findByYoutubeId(args[1]);
                    }
                    if (record != null && record.id > 0) {
                        if (player.canUseVoiceCommands(author, userRank)) {
                            player.connectTo(guild.getMember(author).getVoiceState().getChannel());
                            player.addToQueue(record.filename, author);
                            return Template.get("music_added_to_queue", record.youtubeTitle);
                        }
                    }
                    return Template.get("music_not_added_to_queue", args[1]);
                }
                return Template.get("command_invalid_use");
            case "playtype":
            case "play-type":
                if (args.length == 1) {
                    List<List<String>> tbl = new ArrayList<>();
                    for (OPlaylist.PlayType playType : OPlaylist.PlayType.values()) {
                        if (playType.getId() < 1) continue;
                        tbl.add(Arrays.asList((playType == playlist.getPlayType() ? "*" : " ") + playType.getId(), playType.toString(), playType.getDescription()));
                    }
                    return "the play-type of the playlist. A `*` indicates the selected option" + BotConfig.EOL +
                            Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + BotConfig.EOL +
                            "Private in a guild-setting refers to users with admin privileges, use the number in the first column to set it";
                }
                if (args.length > 1 && args[1].matches("^\\d+$")) {
                    OPlaylist.PlayType playType = OPlaylist.PlayType.fromId(Integer.parseInt(args[1]));
                    playlist.setPlayType(playType);
                    CPlaylist.update(playlist);
                    player.setActivePlayListId(playlist.id);
                    return Template.get(channel, "playlist_setting_updated", "play-type", args[1]);
                }
                return Template.get("playlist_setting_not_numeric", "play-type");

        }
        return Template.get("command_invalid_use");
    }

    /**
     * @see PlaylistCommand#canEditPlaylist(OPlaylist, TextChannel, User, SimpleRank, boolean)
     */
    private boolean canAddTracks(OPlaylist playlist, TextChannel channel, User invoker, SimpleRank userRank) {
        return canEditPlaylist(playlist, channel, invoker, userRank, true);
    }

    /**
     * @see PlaylistCommand#canEditPlaylist(OPlaylist, TextChannel, User, SimpleRank, boolean)
     */
    private boolean canRemoveTracks(OPlaylist playlist, TextChannel channel, User invoker, SimpleRank userRank) {
        return canEditPlaylist(playlist, channel, invoker, userRank, false);
    }

    /**
     * Check if a user has admin privilege on a playlist
     *
     * @param playlist the playlist to check
     * @param channel  the channel where its invoked
     * @param invoker  the user who invoked
     * @param userRank rank of the user
     * @return
     */
    private boolean isPlaylistAdmin(OPlaylist playlist, TextChannel channel, User invoker, SimpleRank userRank) {
        if (playlist.isGlobalList() && userRank.isAtLeast(SimpleRank.CREATOR)) {
            return false;
        }
        if (playlist.isGuildList()) {
            return userRank.isAtLeast(SimpleRank.GUILD_ADMIN);
        }
        if (playlist.isPersonal()) {
            return CUser.getCachedId(invoker.getId()) == playlist.ownerId;
        }
        return userRank.isAtLeast(SimpleRank.CREATOR);
    }

    /**
     * can an invoker add/remove tracks?
     *
     * @param playlist the playlist to check
     * @param channel  the channel where its invoked
     * @param invoker  the user who invoked
     * @param userRank rank of the user
     * @param isAdding adding or removing? false for removing
     * @return can the user add/remove tracks?
     */
    private boolean canEditPlaylist(OPlaylist playlist, TextChannel channel, User invoker, SimpleRank userRank, boolean isAdding) {
        switch (playlist.getEditType()) {
            case PUBLIC_AUTO:
            case PUBLIC_FULL:
                return true;
            case PRIVATE_AUTO:
                if (playlist.isGuildList()) {
                    if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
                        return false;
                    }
                } else if (playlist.isPersonal()) {
                    return CUser.getCachedId(invoker.getId()) == playlist.ownerId;
                }
            case PUBLIC_ADD:
                if (!isAdding) {
                    if (playlist.isGuildList()) {
                        return userRank.isAtLeast(SimpleRank.GUILD_ADMIN);
                    } else if (playlist.isPersonal()) {
                        return CUser.getCachedId(invoker.getId()) == playlist.ownerId;
                    }
                    return false;
                }
                return true;
            case PRIVATE:
                if (playlist.isGuildList() && playlist.guildId == CGuild.getCachedId(channel.getGuild().getId()) && userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
                    return true;
                } else if (playlist.isPersonal()) {
                    return CUser.getCachedId(invoker.getId()) == playlist.ownerId;
                }
                return false;
        }
        return false;
    }

    private OPlaylist findPlaylist(String search, String code, User user, Guild guild) {
        int userId;
        int guildId = CGuild.getCachedId(guild.getId());
        OPlaylist playlist;
        String title;
        switch (search.toLowerCase()) {
            case "mine":
                title = user.getName() + "'s " + code + " list";
                userId = CUser.getCachedId(user.getId(), user.getName());
                playlist = CPlaylist.findBy(userId, 0, code);
                break;
            case "guild":
                title = EmojiParser.parseToAliases(guild.getName()) + "'s " + code + " list";
                playlist = CPlaylist.findBy(0, guildId, code);
                break;
            case "global":
            default:
                title = "Global";
                playlist = CPlaylist.findBy(0, 0);
                break;
        }
        if (playlist.id == 0) {
            playlist.title = title;
            playlist.code = code;
            if (playlist.isPersonal()) {
                playlist.setEditType(OPlaylist.EditType.PRIVATE_AUTO);
            }
            CPlaylist.insert(playlist);
        }
        return playlist;
    }

    private String makeSettingsTable(OPlaylist playlist) {
        List<List<String>> body = new ArrayList<>();
        String owner = playlist.isGlobalList() ? "Emily" : playlist.isGuildList() ? CGuild.findById(playlist.guildId).name : CUser.findById(playlist.ownerId).name;
        body.add(Arrays.asList("Title", playlist.title));
        if (!playlist.isGlobalList()) {
            body.add(Arrays.asList("code", playlist.code));
        }
        body.add(Arrays.asList("Owner", owner));
        body.add(Arrays.asList("edit-type", playlist.getEditType().getDescription()));
        body.add(Arrays.asList("play-type", playlist.getPlayType().getDescription()));
//		body.add(Arrays.asList("visibility", playlist.getVisibility().getDescription()));
//		body.add(Arrays.asList("created", TimeUtil.formatYMD(playlist.createdOn)));
        return Misc.makeAsciiTable(Arrays.asList("Name", "Value"), body, null);
    }

    private String makePage(Guild guild, OPlaylist playlist, int currentPage, int maxPage) {
        List<OMusic> items = CPlaylist.getMusic(playlist.id, ITEMS_PER_PAGE, (currentPage - 1) * ITEMS_PER_PAGE);
        if (items.isEmpty()) {
            return "The playlist is empty!";
        }
        String playlistTable = BotConfig.EOL;
        for (OMusic item : items) {
            playlistTable += String.format("`%11s` | %s" + BotConfig.EOL, item.youtubecode, item.youtubeTitle);
        }
        return String.format("Music in the playlist: %s" + BotConfig.EOL, playlist.title) +
                playlistTable + BotConfig.EOL +
                String.format("Showing [page %s/%s]", currentPage, maxPage) + BotConfig.EOL + BotConfig.EOL +
                "_You can use the `#` to remove an item from the playlist._" + BotConfig.EOL + BotConfig.EOL +
                "_Example:_ `" + DisUtil.getCommandPrefix(guild) + "pl del QnTYIBU7Ueg`";
    }

    @Override
    public CommandReactionListener<PaginationInfo<OPlaylist>> getReactionListener(String userId, PaginationInfo<OPlaylist> initialData) {
        CommandReactionListener<PaginationInfo<OPlaylist>> listener = new CommandReactionListener<>(userId, initialData);
        listener.setExpiresIn(TimeUnit.MINUTES, 2);
        listener.registerReaction(Emojibet.PREV_TRACK, o -> {
            if (listener.getData().previousPage()) {
                String txt = makePage(initialData.getGuild(), initialData.getExtra(), listener.getData().getCurrentPage(), listener.getData().getMaxPage());
                if (txt.length() > 2000) {
                    txt = txt.substring(0, 1999);
                }
                o.editMessage(txt).complete();
            }
        });
        listener.registerReaction(Emojibet.NEXT_TRACK, o -> {
            if (listener.getData().nextPage()) {
                String txt = makePage(initialData.getGuild(), initialData.getExtra(), listener.getData().getCurrentPage(), listener.getData().getMaxPage());
                if (txt.length() > 2000) {
                    txt = txt.substring(0, 1999);
                }
                o.editMessage(txt).complete();
            }
        });
        return listener;
    }
}