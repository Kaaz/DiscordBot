package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OMusic;
import discordbot.db.model.OMusicVote;
import discordbot.db.table.TMusic;
import discordbot.db.table.TMusicVote;
import discordbot.guildsettings.defaults.SettingMusicShowListeners;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * !current
 * retrieves information about the currently playing track
 */
public class CurrentTrack extends AbstractCommand {
	private static final Pattern votePattern = Pattern.compile("^(?>vote|rate)\\s?(\\d+)?$");
	private final String BLOCK_INACTIVE = "▬";
	private final String BLOCK_ACTIVE = ":radio_button:";
	private final String SOUND_CHILL = ":sound:";
	private final String SOUND_LOUD = ":loud_sound:";
	private final float SOUND_TRESHHOLD = 0.4F;
	private final int BLOCK_PARTS = 10;

	public CurrentTrack() {
		super();
	}

	@Override
	public String getDescription() {
		return "retrieves information about the song currently playing";
	}

	@Override
	public String getCommand() {
		return "current";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"current               //info about the currently playing song",
				"current vote <1-10>   //Cast your vote to the song; 1=worst, 10=best",
				"current ban           //bans the current track from being randomly played",
//				"current artist        //sets the artist of current song",
//				"current correct       //accept the systems suggestion of title/artist",
//				"current reversed      //accept the systems suggestion in reverse [title=artist,artist=title]",
//				"current title <title> //sets title of current song",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{"playing", "np", "nowplaying"};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		boolean helpedOut = false;
		Guild guild = ((TextChannel) channel).getGuild();
		OMusic song = TMusic.findById(bot.getCurrentlyPlayingSong(guild));
		if (song.id == 0) {
			return Template.get("command_currentlyplaying_nosong");
		}
		boolean titleIsEmpty = song.title == null || song.title.isEmpty();
		boolean artistIsEmpty = song.artist == null || song.artist.isEmpty();
		String guessTitle = "";
		String guessArtist = "";
		String songTitle;
		if (titleIsEmpty || artistIsEmpty) {
			songTitle = song.youtubeTitle;
		} else {
			songTitle = song.artist + " - " + song.title;
		}

		if (song.youtubeTitle.toLowerCase().chars().filter(e -> e == '-').count() >= 1) {
			String[] splitTitle = song.youtubeTitle.split("-");
			guessTitle = splitTitle[splitTitle.length - 1].trim();
			guessArtist = splitTitle[splitTitle.length - 2].trim();
		}

		if (args.length > 0) {
			String voteInput = args[0].toLowerCase();
			if (args.length > 1) {
				voteInput += " " + args[1];
			}
			Matcher m = votePattern.matcher(voteInput);
			if (m.find()) {
				OMusicVote voteRecord = TMusicVote.findBy(song.id, author.getId());
				if (m.group(1) != null) {
					int vote = Math.max(1, Math.min(10, Integer.parseInt(m.group(1))));
					TMusicVote.insertOrUpdate(song.id, author.getId(), vote);
					return "vote is registered (" + vote + ")";
				}
				if (voteRecord.vote > 0) {
					return Template.get("music_your_vote", songTitle, voteRecord.vote);
				} else {
					return Template.get("music_not_voted", DisUtil.getCommandPrefix(channel) + "np vote ");
				}
			}
		}

		if (args.length >= 1 && bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
			String value = "";
			for (int i = 1; i < args.length; i++) {
				value += args[i] + " ";
			}
			value = value.trim();
			switch (args[0].toLowerCase()) {
				case "ban":
					song.banned = 1;
					TMusic.update(song);
					return Template.get("command_current_banned_success");
				case "title":
					song.title = value;
					TMusic.update(song);
					helpedOut = true;
					break;
				case "artist":
					song.artist = value;
					TMusic.update(song);
					helpedOut = true;
					break;
				case "correct":
					song.artist = guessArtist;
					song.title = guessTitle;
					TMusic.update(song);
					helpedOut = true;
					break;
				case "reversed":
					song.artist = guessTitle;
					song.title = guessArtist;
					TMusic.update(song);
					helpedOut = true;
					break;
				default:
					return Template.get("invalid_command_use");
			}
			titleIsEmpty = song.title == null || song.title.isEmpty();
			artistIsEmpty = song.artist == null || song.artist.isEmpty();
		}
		String ret = "Currently playing " + ":notes: ";
		ret += songTitle;
		ret += Config.EOL + Config.EOL;
		MusicPlayerHandler musicHandler = MusicPlayerHandler.getFor(guild, bot);
		ret += getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume()) + Config.EOL + Config.EOL;

		if (GuildSettings.get(guild).getOrDefault(SettingMusicShowListeners.class).equals("true")) {
			List<User> userList = musicHandler.getUsersInVoiceChannel();
			if (userList.size() > 0) {
				ret += ":headphones:  Listeners" + Config.EOL;
				ArrayList<String> displayList = userList.stream().map(User::getUsername).collect(Collectors.toCollection(ArrayList::new));
				ret += Misc.makeTable(displayList);
			}
		}
		if (bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN) && (titleIsEmpty || artistIsEmpty)) {
			ret += "I am missing some information about this song. Could you help me out:question:" + Config.EOL;
			ret += "If you know the title or artist of this song type **current artist <name>** or **current title <name>**" + Config.EOL;
			if (!titleIsEmpty) {
				ret += "Title: " + song.title + Config.EOL;
			}
			if (!artistIsEmpty) {
				ret += "Artist: " + song.artist + Config.EOL;
			}
			if (!helpedOut && !"".equals(guessArtist) && !"".equals(guessTitle)) {
				ret += Config.EOL + "If I can make a guess:" + Config.EOL;
				ret += "artist: **" + guessArtist + "**" + Config.EOL;
				ret += "title: **" + guessTitle + "**" + Config.EOL;
				ret += "If thats correct type **" + DisUtil.getCommandPrefix(channel) + "np correct** or if its reversed **" + DisUtil.getCommandPrefix(channel) + "np reversed**";

			}
			if (helpedOut) {
				ret += "Thanks for helping out " + author.getAsMention() + "! Have a :cookie:!";
			}
		}
		return ret;
	}

	/**
	 * @param startTime timestamp (in seconds) of the moment the song started playing
	 * @param duration  current song length in seconds
	 * @param volume    volume of the player
	 * @return a formatted mediaplayer
	 */
	private String getMediaplayerProgressbar(long startTime, long duration, float volume) {
		long current = System.currentTimeMillis() / 1000 - startTime;
		String bar = ":pause_button: ";
		int activeBLock = (int) ((float) current / (float) duration * (float) BLOCK_PARTS);
		for (int i = 0; i < BLOCK_PARTS; i++) {
			if (i == activeBLock) {
				bar += BLOCK_ACTIVE;
			} else {
				bar += BLOCK_INACTIVE;
			}
		}
		bar += " [" + Misc.getDurationString(current) + "/" + Misc.getDurationString(duration) + "] ";
		if (volume >= SOUND_TRESHHOLD) {
			bar += SOUND_LOUD;
		} else {
			bar += SOUND_CHILL;
		}
		return bar;
	}
}