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
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * !playlist
 * shows the current songs in the queue
 */
public class Playlist extends AbstractCommand {

	public Playlist() {
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
//				"playlist mine                        //use your playlist",
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
//				"playlist add <youtubelink>           //adds the link to the playlist",
				"playlist remove                      //removes the currently playing music",
//				"playlist remove <youtubelink>        //removes song from playlist",
//				"playlist removeall                   //removes ALL songs from playlist",
				"",
				"-- Changing the settings of the playlist",
				"playlist title <new title>           //edit the playlist title",
				"playlist edit <new type>             //change the edit-type of a playlist",
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
		int listId = player.getActivePLaylistId();
		OPlaylist playlist;
		if (listId > 0) {
			playlist = CPlaylist.findById(listId);
		} else {
			playlist = new OPlaylist();
		}
		if (args.length == 0) {
			if (playlist.isGlobalList()) {
				return Template.get(channel, "music_playlist_using", playlist.title);
			}
			return Template.get(channel, "music_playlist_using", playlist.title) +
					"Settings " + makeSettingsTable(playlist) +
					"To add the currently playing music to the playlist use `" + DisUtil.getCommandPrefix(channel) + "pl add`, check out `" + DisUtil.getCommandPrefix(channel) + "help pl` for more info";
		} else {
			if (args.length >= 1) {
				boolean isAdding = false;
				switch (args[0].toLowerCase()) {
					case "mine":
//						playlist = findPlaylist("mine", author, guild);
//						player.setActivePlayListId(playlist.id);
//						return Template.get(channel, "music_playlist_changed", playlist.title);
						return "Only global and guild lists for now, sorry!";
					case "guild":
						if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
							return Template.get(channel, "no_permission");
						}
						playlist = findPlaylist("guild", author, guild);
						player.setActivePlayListId(playlist.id);
						return Template.get(channel, "music_playlist_changed", playlist.title);
					case "global":
						if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
							return Template.get(channel, "no_permission");
						}
						playlist = findPlaylist("global", author, guild);
						player.setActivePlayListId(playlist.id);
						return Template.get(channel, "music_playlist_changed", playlist.title);
					case "add":
					case "+":
						isAdding = true;
					case "remove":
					case "del":
					case "-":
						if (playlist.isGlobalList()) {
							return Template.get(channel, "playlist_global_readonly");
						}
						if (playlist.isPersonal()) {
							return "Personal lists aren't implemented yet, sorry!";
						}
						if (args.length >= 2 && args[1].matches("^\\d+$")) {
							musicRec = CMusic.findById(Integer.parseInt(args[1]));
							nowPlayingId = musicRec.id;
						}
						if (nowPlayingId == 0) {
							return Template.get(channel, "command_currentlyplaying_nosong");
						}
						switch (playlist.getEditType()) {
							case PRIVATE_AUTO:
								if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
									return Template.get(channel, "no_permission");
								}
							case PUBLIC_AUTO:
								if (isAdding) {
									CPlaylist.addToPlayList(playlist.id, nowPlayingId);
									return Template.get(channel, "playlist_music_added", musicRec.youtubeTitle);
								} else {
									CPlaylist.removeFromPlayList(playlist.id, nowPlayingId);
									return Template.get(channel, "playlist_music_removed");
								}

							case PUBLIC_FULL:
								if (!isAdding) {
									CPlaylist.removeFromPlayList(playlist.id, nowPlayingId);
									return Template.get(channel, "playlist_music_removed", musicRec.youtubeTitle, playlist.title);
								}
							case PUBLIC_ADD:
								if (!isAdding) {
									if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
										CPlaylist.removeFromPlayList(playlist.id, nowPlayingId);
										return Template.get(channel, "playlist_music_removed", musicRec.youtubeTitle, playlist.title);
									}
									return Template.get(channel, "no_permission");
								}
								if (CPlaylist.isInPlaylist(playlist.id, nowPlayingId)) {
									return Template.get(channel, "playlist_music_already_added", musicRec.youtubeTitle, playlist.title);
								}
								CPlaylist.addToPlayList(playlist.id, nowPlayingId);
								return Template.get(channel, "playlist_music_added", musicRec.youtubeTitle, playlist.title);
							case PRIVATE:
								if (playlist.isGuildList() && playlist.guildId != CGuild.getCachedId(guild.getId())) {
									return Template.get(channel, "no_permission");
								}
								if (!isAdding) {
									CPlaylist.removeFromPlayList(playlist.id, nowPlayingId);
									return Template.get(channel, "playlist_music_removed", musicRec.youtubeTitle, playlist.title);
								} else {
									if (CPlaylist.isInPlaylist(playlist.id, nowPlayingId)) {
										return Template.get(channel, "playlist_music_already_added", musicRec.youtubeTitle, playlist.title);
									}
									CPlaylist.addToPlayList(playlist.id, nowPlayingId);
									return Template.get(channel, "playlist_music_added", musicRec.youtubeTitle, playlist.title);
								}
						}
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
			}
			if (args.length < 1) {
				return makeSettingsTable(playlist);
			} else {
				if (playlist.isGlobalList()) {
					return Template.get(channel, "playlist_global_readonly");
				}
				if (playlist.isPersonal()) {
					return "Personal playlists are not fully done yet, sorry!";
				}
				if (playlist.isGuildList() && !userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
					return Template.get(channel, "playlist_title_no_permission");
				}
				switch (args[0].toLowerCase()) {
					case "title":
						if (args.length == 1) {
							return Template.get(channel, "command_playlist_title", playlist.title);
						}
						playlist.title = Joiner.on(" ").join(Arrays.copyOfRange(args, 1, args.length));
						CPlaylist.update(playlist);
						player.setActivePlayListId(playlist.id);
						return Template.get(channel, "playlist_title_updated", playlist.title);
					case "edit-type":
					case "edittype":
					case "edit":
						if (args.length == 1) {
							List<List<String>> tbl = new ArrayList<>();
							for (OPlaylist.EditType editType : OPlaylist.EditType.values()) {
								if (editType.getId() < 1) continue;
								tbl.add(Arrays.asList((editType == playlist.getEditType() ? "*" : " ") + editType.getId(), editType.toString(), editType.getDescription()));
							}
							return "the edit-type of the playlist. A `*` indicates the selected option" + Config.EOL +
									Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + Config.EOL +
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
						if (args.length == 1) {
							List<List<String>> tbl = new ArrayList<>();
							for (OPlaylist.Visibility visibility : OPlaylist.Visibility.values()) {
								if (visibility.getId() < 1) continue;
								tbl.add(Arrays.asList((visibility == playlist.getVisibility() ? "*" : " ") + visibility.getId(), visibility.toString(), visibility.getDescription()));
							}
							return "the visibility-type of the playlist. A `*` indicates the selected option" + Config.EOL +
									Misc.makeAsciiTable(Arrays.asList("#", "Code", "Description"), tbl, null) + Config.EOL +
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

				}
			}

		}
		return Template.get("command_invalid_use");
	}

	private OPlaylist findPlaylist(String search, User user, Guild guild) {
		int userId;
		int guildId = CGuild.getCachedId(guild.getId());
		OPlaylist playlist;
		String title;
		switch (search.toLowerCase()) {
			case "mine":
				title = user.getUsername() + "'s list";
				userId = CUser.getCachedId(user.getId(), user.getUsername());
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
//		body.add(Arrays.asList("visibility", playlist.getVisibility().getDescription()));
//		body.add(Arrays.asList("created", TimeUtil.formatYMD(playlist.createdOn)));
		return Misc.makeAsciiTable(Arrays.asList("Name", "Value"), body, null);
	}
}