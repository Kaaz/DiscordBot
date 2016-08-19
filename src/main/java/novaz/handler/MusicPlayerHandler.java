package novaz.handler;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MusicPlayerHandler {
	private final static Map<IGuild, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
	private final IGuild guild;
	private final NovaBot bot;
	private OMusic currentlyPlaying = new OMusic();
	private IMessage activeMsg;
	private long currentSongLength = 0;
	private long currentSongStartTimeInSeconds = 0;

	private MusicPlayerHandler(IGuild guild, NovaBot bot) {
		this.guild = guild;
		this.bot = bot;
		playerInstances.put(guild, this);
	}

	public OMusic getCurrentlyPlaying() {
		return currentlyPlaying;
	}

	public static MusicPlayerHandler getAudioPlayerForGuild(IGuild guild, NovaBot bot) {
		if (playerInstances.containsKey(guild)) {
			return playerInstances.get(guild);
		} else {
			return new MusicPlayerHandler(guild, bot);
		}
	}

	/**
	 * When did the currently playing song start?
	 *
	 * @return timestamp in seconds
	 */
	public long getCurrentSongStartTime() {
		return currentSongStartTimeInSeconds;
	}

	/**
	 * track duration of current song
	 *
	 * @return duration in seconds
	 */
	public long getCurrentSongLength() {
		return currentSongLength;
	}

	/**
	 * Skips currently playing song
	 */
	public void skipSong() {
		clearMessage();
		AudioPlayer ap = AudioPlayer.getAudioPlayerForGuild(guild);
		ap.skip();
		currentlyPlaying = new OMusic();
		if (ap.getPlaylistSize() == 0) {
			playRandomSong();
		}
	}

	/**
	 * retreives a random .mp3 file from the music directory
	 *
	 * @return filename
	 * @todo make it less random
	 */
	private String getRandomSong() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		return fileList[(int) (Math.random() * (double) fileList.length)];
	}

	/**
	 * A track has ended
	 *
	 * @param oldTrack  track which just stopped
	 * @param nextTrack next track
	 */
	public void onTrackEnded(AudioPlayer.Track oldTrack, Optional<AudioPlayer.Track> nextTrack) {
		clearMessage();
		currentSongLength = 0;
		currentlyPlaying = new OMusic();
		if (!nextTrack.isPresent()) {
			playRandomSong();
		}
	}

	/**
	 * a track has started
	 *
	 * @param track the track which has started
	 */
	public void onTrackStarted(AudioPlayer.Track track) {
		clearMessage();
		Map<String, Object> metadata = track.getMetadata();
		String msg = "Now playing unknown file :(";
		if (metadata.containsKey("file")) {
			if (metadata.get("file") instanceof File) {
				File f = (File) metadata.get("file");
				getMp3Details(f);
				OMusic music = TMusic.findByFileName(f.getName());
				currentlyPlaying = music;
				currentSongStartTimeInSeconds = System.currentTimeMillis() / 1000;
				if (music.title.isEmpty()) {
					msg = "plz send help:: " + f.getName();
				} else {
					msg = "Now playing " + music.title;
				}
			}
		}
		activeMsg = bot.sendMessage(bot.getDefaultChannel(guild), msg);
	}

	private void getMp3Details(File f) {
		try {
			Mp3File mp3file = new Mp3File(f);
			currentSongLength = mp3file.getLengthInSeconds();
		} catch (IOException | InvalidDataException | UnsupportedTagException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes 'now playing' message if it exists
	 */
	private void clearMessage() {
		if (activeMsg != null) {
			try {
				activeMsg.delete();
				activeMsg = null;
			} catch (MissingPermissionsException | RateLimitException | DiscordException ignored) {
			}
		}
	}

	/**
	 * Adds a random song from the music directory to the queue
	 *
	 * @return successfully started playing
	 */
	public boolean playRandomSong() {
		String randomSong = getRandomSong();
		guild.getVoiceChannels();
		if (bot.instance.getConnectedVoiceChannels().isEmpty()) {
			return false;
		}
		List<IUser> usersInVoiceChannel = getUsersInVoiceChannel();
		if (usersInVoiceChannel.isEmpty()) {
			return false;
		}
		try {
			AudioPlayer.getAudioPlayerForGuild(guild).queue(new File(Config.MUSIC_DIRECTORY + randomSong));
			return true;
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();

		}
		return false;
	}

	public List<IUser> getUsersInVoiceChannel() {
		ArrayList<IUser> userList = new ArrayList<>();
		List<IVoiceChannel> connectedVoiceChannels = bot.instance.getOurUser().getConnectedVoiceChannels();
		IVoiceChannel currentChannel = null;
		for (IVoiceChannel channel : connectedVoiceChannels) {
			if (channel.getGuild().equals(guild)) {
				currentChannel = channel;
				break;
			}
		}
		if (currentChannel != null) {
			List<IUser> connectedUsers = currentChannel.getConnectedUsers();
			for (IUser user : connectedUsers) {
				if (!user.equals(bot.instance.getOurUser())) {
					userList.add(user);
				}
			}
		}
		return userList;
	}

	/**
	 * Clears existing message and stops playing music for guild
	 */

	public void stopMusic() {
		clearMessage();
		AudioPlayer.getAudioPlayerForGuild(guild).clear();
	}

	public float getVolume() {
		return AudioPlayer.getAudioPlayerForGuild(guild).getVolume();
	}
}
