package discordbot.command.music;

import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OPlaylist;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Misc;
import discordbot.util.TimeUtil;
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
				"playlist mine                        //use your playlist",
				"playlist guild                       //use the guild's playlist",
				"playlist global                      //use the global playlist",
				"playlist settings                    //check the settings for the active playlist",
				"playlist settings <playlistname>     //check the settings for the active playlist",
				"playlist                             //info about the current playlist",
				"playlist list                        //see what playlists there are",
				"",
				"-- Adding and removing music from the playlist",
				"playlist show <pagenumber>           //shows music in the playlist",
				"playlist add                         //adds the currently playing song",
				"playlist add <youtubelink>           //adds the link to the playlist",
				"playlist remove                      //removes the currently playing song",
				"playlist remove <youtubelink>        //removes song from playlist",
				"playlist removeall                   //removes ALL songs from playlist",
				"",
				"-- Changing the settings of the playlist",
				"playlist title <new title>           //edit the playlist title",
				"playlist edit-type <new type>        //change the edit-type of a playlist",
				"playlist visibility <new visibility> //change who can see the playlist",
				"playlist reset                       //reset settings to default",
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
			return Template.get(channel, "music_playlist_using", playlist.title);
		} else {
			if (args.length == 1) {
				switch (args[0].toLowerCase()) {
					case "mine":
						playlist = findPlaylist("mine", author, guild);
						player.setActivePlayListId(playlist.id);
						return Template.get(channel, "music_playlist_changed", playlist.title);
					case "guild":
						playlist = findPlaylist("guild", author, guild);
						player.setActivePlayListId(playlist.id);
						return Template.get(channel, "music_playlist_changed", playlist.title);
					case "global":
					default:
						playlist = findPlaylist("global", author, guild);
						player.setActivePlayListId(playlist.id);
						return Template.get(channel, "music_playlist_changed", playlist.title);
				}
			}
			switch (args[0].toLowerCase()) {
				case "use":
					break;
				case "setting":
				case "settings":
					if (args.length < 3) {
						if (args.length > 1) {
							return makeSettingsTable(findPlaylist(args[1], author, guild));
						}
						return makeSettingsTable(playlist);
					}
					break;
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
		String owner = playlist.ownerId == 0 ? "Guild" : CUser.findById(playlist.ownerId).name;
		body.add(Arrays.asList("Title", playlist.title));
		body.add(Arrays.asList("Owner", owner));
		body.add(Arrays.asList("edit-type", playlist.getEditType().getDescription()));
		body.add(Arrays.asList("visibility", playlist.getVisibility().getDescription()));
		body.add(Arrays.asList("created", TimeUtil.formatYMD(playlist.createdOn)));
		return Misc.makeAsciiTable(Arrays.asList("Name", "Value"), body, null);
	}
}