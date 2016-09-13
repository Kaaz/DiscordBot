package novaz.command.music;

import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import novaz.core.AbstractCommand;
import novaz.db.WebDb;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.handler.MusicPlayerHandler;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.SCUtil;
import novaz.util.YTUtil;
import novaz.util.obj.SCFile;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

	private final Pattern musicResultFilterPattern = Pattern.compile("^#?([0-9]{1,2})$");
	private final Pattern soundCloudUrlPattern = Pattern.compile("^https?://soundcloud.com/([a-z0-9-]+)/(sets/)?([a-z0-9-]+)$");
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
	public boolean isAllowedInPrivateChannel() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"play <youtubelink>       //download and plays song",
				"play <soundcloudlink>    //download and plays song",
				"play <youtubevideocode>  //download and plays song",
				"play <part of title>     //shows search results",
				"play <resultnumber>      //add result # to the queue"
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
			Matcher filterMatch = musicResultFilterPattern.matcher(args[0]);
			if (filterMatch.matches() && userFilteredSongs.containsKey(author.getID())) {
				if (userFilteredSongs.containsKey(author.getID()) && userFilteredSongs.get(author.getID()) != null) {
					int selectedIndex = Ints.tryParse(args[0].replace("#", ""));
					if (userFilteredSongs.get(author.getID()).size() + 1 >= selectedIndex && selectedIndex > 0) {
						int songId = userFilteredSongs.get(author.getID()).get(selectedIndex - 1);
						try (ResultSet rs = WebDb.get().select("SELECT filename, youtube_title, artist FROM playlist WHERE id = ?", songId)) {
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
			Matcher scMatcher = soundCloudUrlPattern.matcher(args[0]);
			if (SCUtil.isEnabled() && scMatcher.matches()) {
				if (SCUtil.download(args[0])) {
					List<SCFile> downloadedList = SCUtil.getDownloadedList();
					String text = "Found **" + downloadedList.size() + "** song(s) and added to the queue. " + Config.EOL;
					for (SCFile scFile : downloadedList) {
						OMusic oMusic = TMusic.findByYoutubeId(scFile.id);
						if (oMusic.id == 0) {
							oMusic.youtubecode = scFile.id;
							oMusic.artist = scFile.artist;
							oMusic.title = scFile.title;
							oMusic.filename = scFile.id + ".mp3";
							oMusic.youtubeTitle = scFile.artist + " - " + scFile.title;
							TMusic.insert(oMusic);
							try {
								Files.move(new File(Config.MUSIC_DIRECTORY + "soundcloud/" + scFile.filename), new File(Config.MUSIC_DIRECTORY + oMusic.filename));
							} catch (IOException e) {
								e.printStackTrace();
								bot.out.sendErrorToMe(e, "moving file", Config.MUSIC_DIRECTORY + "soundcloud/" + scFile.filename, "target", Config.MUSIC_DIRECTORY + oMusic.filename, bot);
								continue;
							}
						}
						text += String.format("%s - %s", oMusic.artist, oMusic.title) + Config.EOL;
						bot.addSongToQueue(oMusic.filename, channel.getGuild());
					}
					return text;
				}
				return TextHandler.get("music_download_soundcloud_failed");
			}

			String videocode = YTUtil.extractCodeFromUrl(args[0]);
			if (YTUtil.isValidYoutubeCode(videocode)) {
				File filecheck = new File(Config.MUSIC_DIRECTORY + videocode + ".mp3");
				if (!filecheck.exists()) {
					IMessage msg = bot.out.sendMessage(channel, TextHandler.get("music_downloading_hang_on"));
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
						rec.youtubeTitle = YTUtil.getTitleFromPage(videocode);
						rec.youtubecode = videocode;
						rec.filename = videocode + ".mp3";
						TMusic.update(rec);
						bot.addSongToQueue(videocode + ".mp3", channel.getGuild());
						return ":notes: Found *" + rec.youtubeTitle + "* And added it to the queue";
					}
					bot.addSongToQueue(videocode + ".mp3", channel.getGuild());
					return TextHandler.get("music_added_to_queue");
				}
			} else {
				String concatArgs = "";
				for (String s : args) {
					concatArgs += s.toLowerCase();
				}
				try (ResultSet rs = WebDb.get().select("SELECT id, GREATEST(levenshtein_ratio(LOWER(title),?),levenshtein_ratio(LOWER(artist),?)) AS matchrating, youtube_title,title,artist, filename " +
						"FROM playlist " +
						"WHERE artist IS NOT NULL AND title IS NOT NULL " +
						"ORDER BY matchrating DESC " +
						"LIMIT 10", concatArgs, concatArgs)) {
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
						results += String.format("%2s %7s %s - %s", i, rs.getInt("matchrating"), rs.getString("artist"), rs.getString("title")) + Config.EOL;
					}
					if (!results.isEmpty()) {
						userFilteredSongs.put(author.getID(), songIdArray);

						return "Results ```" + Config.EOL +
								String.format("%2s %7s %s - %s", "#", "match %", "artist", "title") + Config.EOL +
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