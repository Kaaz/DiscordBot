package discordbot.command.music;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CMusic;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OMusic;
import discordbot.db.model.OPlaylist;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * !playlist
 * shows the current songs in the queue
 */
public class PlaylistCommand extends AbstractCommand {

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
				"playlist mine                        //use your playlist",
				"playlist guild                       //use the guild's playlist",
				"playlist global                      //use the global playlist",
				"playlist settings                    //check the settings for the active playlist",
//				"playlist settings <playlistname>     //check the settings for the active playlist",
				"playlist                             //info about the current playlist",
				"playlist list <pagenumber>           //Shows the music in the playlist",
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
			return Template.get(channel, "music_required_role_not_found", GuildSettings.getFor(channel, SettingMusicRole.class));
		}
		OPlaylist playlist = CPlaylist.findById(player.getActivePLaylistId());
		if (playlist.id == 0) {
			return "";
		}
		if (args.length == 0) {
			if (playlist.isGlobalList()) {
				return Template.get(channel, "music_playlist_using", playlist.title) + " See `" + DisUtil.getCommandPrefix(channel) + "pl help` for more info";
			}
			return Template.get(channel, "music_playlist_using", playlist.title) +
					"Settings " + makeSettingsTable(playlist) +
					"To add the currently playing music to the playlist use `" + DisUtil.getCommandPrefix(channel) + "pl add`, check out `" + DisUtil.getCommandPrefix(channel) + "help pl` for more info";
		}
		OPlaylist newlist = null;
		switch (args[0].toLowerCase()) {
			case "mine":
				newlist = findPlaylist("mine", author, guild);
				break;
			case "guild":
				newlist = findPlaylist("guild", author, guild);
				break;
			case "global":
				newlist = findPlaylist("global", author, guild);
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
				int currentPage = 0;
				int itemsPerPage = 20;
				int totalTracks = CPlaylist.getMusicCount(playlist.id);
				int maxPage = 1 + totalTracks / itemsPerPage;
				if (args.length >= 2) {
					if (args[1].matches("^\\d+$")) {
						currentPage = Math.min(Math.max(0, Integer.parseInt(args[1]) - 1), maxPage - 1);
					}
				}
				List<OMusic> items = CPlaylist.getMusic(playlist.id, itemsPerPage, currentPage * itemsPerPage);
				if (items.isEmpty()) {
					return "The playlist is empty.";
				}
				String playlistTable = Config.EOL;
				for (OMusic item : items) {
					playlistTable += String.format(":hash: `%6s` \uD83D\uDD39 %s" + Config.EOL, item.id, item.youtubeTitle);
				}
				return String.format("Music in the playlist: %s" + Config.EOL, playlist.title) +
						playlistTable + Config.EOL +
						String.format("Showing [page %s/%s] (in total: %s items)", currentPage + 1, maxPage, totalTracks) + Config.EOL + Config.EOL +
						"_You can use the `#` to remove an item from the playlist._" + Config.EOL + Config.EOL +
						"_Example:_ `" + DisUtil.getCommandPrefix(channel) + "pl del 123`";

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
					return "the edit-type of the playlist. A `*` indicates the selected option" + Config.EOL +
							Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + Config.EOL +
							"To change the type use the \\#, for instance `" + DisUtil.getCommandPrefix(channel) + "pl edit 3` sets it to PUBLIC_ADD " + Config.EOL + Config.EOL +
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
					return "the visibility-type of the playlist. A `*` indicates the selected option" + Config.EOL +
							Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + Config.EOL +
							"To change the type use the \\#, for instance `" + DisUtil.getCommandPrefix(channel) + "pl vis 3` sets it to GUILD " + Config.EOL + Config.EOL +
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
				if (args.length > 1 && args[1].matches("^\\d+$")) {
					OMusic record = CMusic.findById(Integer.parseInt(args[1]));
					if (record.id > 0) {
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
					return "the play-type of the playlist. A `*` indicates the selected option" + Config.EOL +
							Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + Config.EOL +
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

	private OPlaylist findPlaylist(String search, User user, Guild guild) {
		int userId;
		int guildId = CGuild.getCachedId(guild.getId());
		OPlaylist playlist;
		String title;
		switch (search.toLowerCase()) {
			case "mine":
				title = user.getName() + "'s list";
				userId = CUser.getCachedId(user.getId(), user.getName());
				playlist = CPlaylist.findBy(userId);
				break;
			case "guild":
				title = EmojiParser.parseToAliases(guild.getName()) + "'s list";
				playlist = CPlaylist.findBy(0, guildId);
				break;
			case "global":
			default:
				title = "Global";
				playlist = CPlaylist.findBy(0, 0);
				break;
		}
		if (playlist.id == 0) {
			playlist.title = title;
			CPlaylist.insert(playlist);
		}
		return playlist;
	}

	private String makeSettingsTable(OPlaylist playlist) {
		List<List<String>> body = new ArrayList<>();
		String owner = playlist.isGlobalList() ? "Emily" : playlist.isGuildList() ? CGuild.findById(playlist.guildId).name : CUser.findById(playlist.ownerId).name;
		body.add(Arrays.asList("Title", playlist.title));
		body.add(Arrays.asList("Owner", owner));
		body.add(Arrays.asList("edit-type", playlist.getEditType().getDescription()));
		body.add(Arrays.asList("play-type", playlist.getPlayType().getDescription()));
//		body.add(Arrays.asList("visibility", playlist.getVisibility().getDescription()));
//		body.add(Arrays.asList("created", TimeUtil.formatYMD(playlist.createdOn)));
		return Misc.makeAsciiTable(Arrays.asList("Name", "Value"), body, null);
	}
}