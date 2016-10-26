package discordbot.command.music;

import com.google.common.primitives.Ints;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.WebDb;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.YTUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * !oplay
 * old version of the play command, it'll remain for now
 */
public class OldPlay extends AbstractCommand {
	private final Pattern musicResultFilterPattern = Pattern.compile("^#?([0-9]{1,2})$");
	private Map<String, ArrayList<Integer>> userFilteredSongs = new ConcurrentHashMap<>();

	public OldPlay() {
		super();
	}

	@Override
	public String getDescription() {
		return "Plays a song from youtube";
	}

	@Override
	public String getCommand() {
		return "oplay";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"play <part of title>     //shows search results",
				"play <resultnumber>      //add result # to the queue"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		TextChannel tc = (TextChannel) channel;
		if (MusicPlayerHandler.getFor(tc.getGuild(), bot).getUsersInVoiceChannel().size() == 0) {
			return Template.get("music_no_users_in_channel");
		}
		if (args.length > 0) {
			Matcher filterMatch = musicResultFilterPattern.matcher(args[0]);
			if (filterMatch.matches() && userFilteredSongs.containsKey(author.getId())) {
				if (userFilteredSongs.containsKey(author.getId()) && userFilteredSongs.get(author.getId()) != null) {
					int selectedIndex = Ints.tryParse(args[0].replace("#", ""));
					if (userFilteredSongs.get(author.getId()).size() + 1 >= selectedIndex && selectedIndex > 0) {
						int songId = userFilteredSongs.get(author.getId()).get(selectedIndex - 1);
						try (ResultSet rs = WebDb.get().select("SELECT filename, youtube_title, artist FROM music WHERE id = ?", songId)) {
							if (rs.next()) {
								bot.addSongToQueue(rs.getString("filename"), tc.getGuild());
							}
							rs.getStatement().close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						userFilteredSongs.remove(author.getId());
						return Template.get("music_added_to_queue");
					} else {
						return Template.get("command_play_filter_match_no_such_index");
					}
				} else {
					return Template.get("command_play_no_results_saved");
				}
			}
			if (userFilteredSongs.containsKey(author.getId())) {
				userFilteredSongs.remove(author.getId());
			}

			String videocode = YTUtil.extractCodeFromUrl(args[0]);
			if (YTUtil.isValidYoutubeCode(videocode)) {
				File filecheck = new File(Config.MUSIC_DIRECTORY + videocode + ".mp3");
				if (filecheck.exists()) {
					bot.addSongToQueue(videocode + ".mp3", tc.getGuild());
					return Template.get("music_added_to_queue");
				}
			} else {
				String concatArgs = "";
				for (String s : args) {
					concatArgs += s.toLowerCase();
				}
				try (ResultSet rs = WebDb.get().select("SELECT id, GREATEST(levenshtein_ratio(LOWER(title),?),levenshtein_ratio(LOWER(artist),?)) AS matchrating, youtube_title,title,artist, filename " +
						"FROM music " +
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
						userFilteredSongs.get(author.getId());
						results += String.format("%2s %7s %s - %s", i, rs.getInt("matchrating"), rs.getString("artist"), rs.getString("title")) + Config.EOL;
					}
					rs.getStatement().close();
					if (!results.isEmpty()) {
						userFilteredSongs.put(author.getId(), songIdArray);

						return "Results ```" + Config.EOL +
								String.format("%2s %7s %s - %s", "#", "match %", "artist", "title") + Config.EOL +
								results + Config.EOL +
								"```" + Config.EOL +
								"Use the command  **play #<resultnumber>** to add it to the music queue. Eg: **play #1** to play the first result." + Config.EOL;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return Template.get("command_play_no_results");
			}
		} else {
			if (bot.playRandomSong(tc.getGuild())) {
				return Template.get("music_started_playing_random");
			} else {
				return Template.get("music_failed_to_start");
			}
		}
		return Template.get("music_not_added_to_queue");
	}
}