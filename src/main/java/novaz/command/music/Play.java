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
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * !play
 * plays a youtube link
 * yea.. play is probably not a good name at the moment
 */
public class Play extends AbstractCommand {

	private final Pattern musicResultFilterPattern = Pattern.compile("^#[0-9]{1,2}$");
	private Map<String, ArrayList<Integer>> userFilteredSongs = new ConcurrentHashMap<>();

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
				"play <youtubelink>        //download and plays song",
				"play <youtubevideocode>   //download and plays song",
				"play <part of title>      //shows search results",
				"play #<resultnumber>      //add result # to the queue"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
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
			if (args[0].startsWith("#")) {
				Matcher filterMatch = musicResultFilterPattern.matcher(args[0]);
				if (!filterMatch.matches()) {
					return TextHandler.get("command_play_filter_match_invalid");
				}
				if (userFilteredSongs.containsKey(author.getID()) && userFilteredSongs.get(author.getID()) != null) {
					int selectedIndex = Ints.tryParse(args[0].replace("#", ""));
					if (userFilteredSongs.get(author.getID()).size() + 1 >= selectedIndex && selectedIndex > 0) {
						int songId = userFilteredSongs.get(author.getID()).get(selectedIndex - 1);
						try (ResultSet rs = WebDb.get().select("SELECT filename, title, artist FROM playlist WHERE id = ?", songId)) {
							if (rs.next()) {
								bot.addSongToQueue(rs.getString("filename"), channel.getGuild());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
						userFilteredSongs.remove(author.getID());
						return TextHandler.get("music_added_to_queue");
					} else {
						return TextHandler.get("command_play_filter_match_no_such_index");
					}
				} else {
					return TextHandler.get("command_play_no_results_saved");
				}
			}
			if (userFilteredSongs.containsKey(author.getID())) {
				userFilteredSongs.remove(author.getID());
			}
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
				for (String s : args) {
					concatArgs += s;
				}
				try (ResultSet rs = WebDb.get().select("SELECT id, levenshtein_ratio(LOWER(title),?) AS matchrating, title, filename " +
						"FROM playlist " +
						"ORDER BY matchrating DESC " +
						"LIMIT 10", concatArgs)) {
					String results = "";
					int i = 0;
					ArrayList<Integer> songIdArray = new ArrayList<>();
					while (rs.next()) {
						if (rs.getInt("matchrating") < 10) {
							continue;
						}
						i++;
						songIdArray.add(rs.getInt("id"));
						userFilteredSongs.get(author.getID());
						results += String.format("%2s %7s %s", i, rs.getInt("matchrating"), rs.getString("title")) + Config.EOL;
					}
					if (!results.isEmpty()) {
						userFilteredSongs.put(author.getID(), songIdArray);

						return "Results ```" + Config.EOL +
								String.format("%2s %7s %s", "#", "match %", "song") + Config.EOL +
								results + Config.EOL +
								"```" + Config.EOL +
								"Use the command  **play #<resultnumber>** to add it to the music queue. Eg: **play #1** to play the first result." + Config.EOL;
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