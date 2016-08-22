package novaz.command.music;

import com.google.common.primitives.Ints;
import novaz.core.AbstractCommand;
import novaz.db.WebDb;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.handler.MusicPlayerHandler;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.YTUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * !play
 * plays a youtube link
 * yea.. play is probably not a good name at the moment
 */
public class Play extends AbstractCommand {

	public Play(NovaBot b) {
		super(b);
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
	public String[] getUsage() {
		return new String[]{
				"play <youtubelink>                   //download and plays song",
				"play <youtubevideocode>              //download and plays song",
				"play <part of title>                 //shows search results",
				"play <part of title> #<resultnumber> //add result # to the queue"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.instance.getConnectedVoiceChannels().size() == 0) {
			return TextHandler.get("music_not_in_voicechannel");
		}
		if (MusicPlayerHandler.getAudioPlayerForGuild(channel.getGuild(), bot).getUsersInVoiceChannel().size() == 0) {
			return TextHandler.get("music_no_users_in_channel");
		}
		if (args.length > 0) {
			boolean justDownloaded = false;
			String videocode = YTUtil.extractCodeFromUrl(args[0]);
			if (YTUtil.isValidYoutubeCode(videocode)) {
				File filecheck = new File(Config.MUSIC_DIRECTORY + videocode + ".mp3");
				if (!filecheck.exists()) {
					IMessage msg = bot.sendMessage(channel, TextHandler.get("music_downloading_hang_on"));
					YTUtil.downloadfromYoutubeAsMp3(videocode);
					justDownloaded = true;
					try {
						msg.delete();
					} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
						e.printStackTrace();
					}
				}
				if (filecheck.exists()) {
					if (justDownloaded) {
						OMusic rec = TMusic.findByYoutubeId(videocode);
						rec.title = YTUtil.getTitleFromPage(videocode);
						rec.youtubecode = videocode;
						rec.filename = videocode + ".mp3";
						TMusic.update(rec);
						bot.addSongToQueue(videocode + ".mp3", channel.getGuild());
						return ":notes: Found *" + rec.title + "* And added it to the queue";
					}
					bot.addSongToQueue(videocode + ".mp3", channel.getGuild());
					return TextHandler.get("music_added_to_queue");
				}
			} else {
				String concatArgs = "";
				int selectedIndex = -1;
				for (int i = 0; i < args.length; i++) {
					String part = args[i];
					if (part.startsWith("#")) {
						selectedIndex = Ints.tryParse(part.replace("#", ""));
						break;
					}
					//it converts a yt link to an mp3
					concatArgs += " " + part;
				}
				long startTime = System.currentTimeMillis();
				try (ResultSet rs = WebDb.get().select("SELECT levenshtein_ratio(LOWER(title),?) AS matchrating, title, filename " +
						"FROM playlist " +
						"ORDER BY matchrating DESC " +
						"LIMIT 10", concatArgs)) {
					String results = "";
					int i = 0;
					while (rs.next()) {
						i++;
						if (selectedIndex > 0 && i == selectedIndex) {
							bot.addSongToQueue(rs.getString("filename"), channel.getGuild());
							return TextHandler.get("music_added_to_queue") + " " + rs.getString("title");
						}
						results += String.format("%2s %7s %s", i, rs.getInt("matchrating"), rs.getString("title")) + Config.EOL;
					}
					if (!results.isEmpty()) {
						return "Results ```" + Config.EOL +
								String.format("%2s %7s %s", "#", "match %", "song") + Config.EOL +
								results + Config.EOL +
								"```" + Config.EOL +
								"Append **#<resultnumber>** to add it to the music queue. Eg: **#1** to play the first result." + Config.EOL;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return TextHandler.get("command_play_no_results");

			}
		} else {
			if (bot.playRandomSong(channel.getGuild())) {
				return TextHandler.get("music_started_playing_random");
			} else {
				return TextHandler.get("music_failed_to_start");
			}
		}
		return TextHandler.get("music_not_added_to_queue");
	}
}