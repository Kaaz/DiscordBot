package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OPlaylist;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.controllers.CUser;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
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
		if (1 == 1) {//yep I know
			return Template.get("command_disabled");
		}
		int listId = player.getActivePLaylistId();
		OPlaylist playlist;
		if (listId > 0) {
			playlist = CPlaylist.findById(listId);
		} else {
			playlist = new OPlaylist();
		}
		if (args.length == 0) {
			if (playlist.id == 0) {
				return "no playlist active at the moment, using the global list.";
			}
			return "";
		} else {
			if (args.length == 1) {
				switch (args[0].toLowerCase()) {
					case "mine":
						playlist = findPlaylist("mine", author, guild);
						player.setActivePlayListId(playlist.id);
						return "Changed to your playlist!";
					case "guild":
						playlist = findPlaylist("guild", author, guild);
						player.setActivePlayListId(playlist.id);
						return "Changed the guild's playlist!";
					case "global":
						player.setActivePlayListId(0);
						return "Stopped using a playlist!";
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
						if (playlist.id == 0) {
							return "Global playlist has no settings";
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
		if ("mine".equalsIgnoreCase(search)) {
			userId = CUser.getCachedId(user.getId());
		} else if ("guild".equalsIgnoreCase(search)) {
			userId = 0;
		} else {
			return CPlaylist.findBy(CUser.getCachedId(user.getId()));
		}
		OPlaylist playlist = CPlaylist.findBy(userId, guildId);
		if (playlist.id == 0) {
			if (userId > 0) {
				playlist = CPlaylist.findBy(CUser.getCachedId(user.getId()));
			}
			if (playlist.id == 0) {
				playlist.ownerId = userId;
				playlist.guildId = guildId;
				CPlaylist.insert(playlist);
			}
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