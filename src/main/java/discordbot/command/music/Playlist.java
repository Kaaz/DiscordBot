package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import discordbot.util.TimeUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * !playlist
 * shows the current songs in the queue
 */
public class Playlist extends AbstractCommand {

	public Playlist(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "information about the playlist/history";
	}

	@Override
	public String getCommand() {
		return "playlist";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"playlist          //playlist queue",
				"playlist clear    //playlist queue",
				"playlist history  //list of recently played songs"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length == 0) {
			MusicPlayerHandler player = MusicPlayerHandler.getAudioPlayerForGuild(channel.getGuild(), bot);
			List<OMusic> queue = player.getQueue();
			String ret = "Music Queue" + Config.EOL;
			if (queue.size() == 0) {
				ret += "The queue is currently empty. " + Config.EOL +
						"To add a song use the **play** command!";
			} else {
				int i = 0;
				for (OMusic song : queue) {
					i++;
					ret += String.format("#%03d %s", i, song.youtubeTitle) + Config.EOL;
				}
			}
			return ret;
		} else if (args[0].equals("history")) {
			List<OMusic> recentlyPlayed = TMusic.getRecentlyPlayed(10);
			if (recentlyPlayed.size() > 0) {
				String ret = "List of recently played music " + Config.EOL;
				ret += String.format("    %-16s %s", ":watch:", ":notes:") + Config.EOL;
				String tableContent = "";
				for (OMusic song : recentlyPlayed) {
					tableContent += String.format("%-7s%s", TimeUtil.getRelativeTime(song.lastplaydate), song.youtubeTitle) + Config.EOL;
				}
				return ret + Misc.makeTable(tableContent);
			} else {
				return Template.get("music_not_played_anything_yet");
			}
		} else if (args[0].equals("clear")) {
			MusicPlayerHandler player = MusicPlayerHandler.getAudioPlayerForGuild(channel.getGuild(), bot);
			player.clearPlayList();
			return Template.get("music_playlist_cleared");
		}
		return Template.get("command_invalid_use");
	}
}