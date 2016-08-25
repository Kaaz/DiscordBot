package novaz.command.music;

import novaz.core.AbstractCommand;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.handler.MusicPlayerHandler;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import novaz.util.TimeUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * !playlist
 * shows the current songs in the queue
 */
public class Playlist extends AbstractCommand {

	public Playlist(NovaBot b) {
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
				"playlist history  //list of recently played songs"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
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
					ret += String.format("#%03d %s", i, song.title) + Config.EOL;
				}
			}
			return ret;
		} else if (args.length >= 1 && args[0].equals("history")) {
			List<OMusic> recentlyPlayed = TMusic.getRecentlyPlayed(10);
			if (recentlyPlayed.size() > 0) {
				String ret = "List of recently played music " + Config.EOL;
				ret += String.format("    %-16s %s", ":watch:", ":notes:") + Config.EOL;
				String tableContent = "";
				for (OMusic song : recentlyPlayed) {
					tableContent += String.format("%-7s%s", TimeUtil.getTimeAgo(song.lastplaydate), song.title) + Config.EOL;
				}
				return ret + Misc.makeTable(tableContent);
			} else {
				return TextHandler.get("music_not_played_anything_yet");
			}
		}
		return TextHandler.get("command_invalid_use");
	}
}