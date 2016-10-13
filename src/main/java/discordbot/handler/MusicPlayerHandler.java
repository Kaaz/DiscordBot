package discordbot.handler;

import discordbot.db.WebDb;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.guildsettings.defaults.SettingMusicVolume;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.hooks.events.FinishEvent;
import net.dv8tion.jda.player.hooks.events.PlayEvent;
import net.dv8tion.jda.player.hooks.events.SkipEvent;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.LocalSource;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MusicPlayerHandler {
	private final static Map<Guild, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
	private final Guild guild;
	private final DiscordBot bot;
	private OMusic currentlyPlaying = new OMusic();
	private Message activeMsg = null;
	private long currentSongLength = 0;
	private long currentSongStartTimeInSeconds = 0;
	private Random rng;
	private AudioManager manager;
	private MusicPlayer player;

	private MusicPlayerHandler(Guild guild, DiscordBot bot) {
		this.guild = guild;
		this.bot = bot;
		rng = new Random();
		manager = guild.getAudioManager();
		if (manager.getSendingHandler() == null) {
			manager = guild.getAudioManager();
			player = new MusicPlayer();
			manager.setSendingHandler(player);
			player.addEventListener(event -> {
				if (event instanceof SkipEvent || event instanceof FinishEvent) {
					System.out.println("SKIP OR FINISH");
					if (player.getAudioQueue().isEmpty()) {
						playRandomSong();
					}
				}
				if (event instanceof PlayEvent) {
					System.out.println("play event triggered");
					setPlayStartTimeStamp();
				}
			});
		} else {
			player = (MusicPlayer) manager.getSendingHandler();
		}
		player.setVolume(Float.parseFloat(GuildSettings.get(guild).getOrDefault(SettingMusicVolume.class)) / 100F);
		playerInstances.put(guild, this);
	}

	public static MusicPlayerHandler getFor(Guild guild, DiscordBot bot) {
		if (playerInstances.containsKey(guild)) {
			return playerInstances.get(guild);
		} else {
			return new MusicPlayerHandler(guild, bot);
		}
	}

	public long getStartTimeStamp() {
		return currentSongStartTimeInSeconds;
	}

	private void setPlayStartTimeStamp() {
		currentSongStartTimeInSeconds = System.currentTimeMillis() / 1000L;
		AudioInfo info = player.getCurrentAudioSource().getInfo();
		currentSongLength = info.getDuration().getTotalSeconds();
		File f = new File(info.getOrigin());
		currentlyPlaying = TMusic.findByFileName(f.getAbsolutePath());
	}

	public boolean isConnectedTo(VoiceChannel channel) {
		return channel.equals(guild.getAudioManager().getConnectedChannel());
	}

	public void connectTo(VoiceChannel channel) {
		guild.getAudioManager().openAudioConnection(channel);
	}

	public boolean isConnected() {
		return guild.getAudioManager().getConnectedChannel() == null;
	}

	public boolean leave() {
		if (isConnected()) {
			return false;
		}
		guild.getAudioManager().closeAudioConnection();
		return true;
	}

	public void clearPlayList() {
//		AudioPlayer.getAudioPlayerForGuild(guild).getPlaylist().clear();
	}

	public OMusic getCurrentlyPlaying() {
		return this.currentlyPlaying;
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
		player.skipToNext();
		currentlyPlaying = new OMusic();
		currentSongLength = 0;
		currentSongStartTimeInSeconds = 0;
	}

	/**
	 * retreives a random .mp3 file from the music directory
	 *
	 * @return filename
	 */
	private String getRandomSong() {
		ArrayList<String> potentialSongs = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT filename, youtube_title, lastplaydate " +
						"FROM music " +
						"WHERE banned = 0 " +
						"ORDER BY lastplaydate ASC " +
						"LIMIT 50")) {
			while (rs.next()) {
				potentialSongs.add(rs.getString("filename"));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
			bot.out.sendErrorToMe(e, bot);
		}
		return potentialSongs.get(rng.nextInt(potentialSongs.size()));
	}

	/**
	 * Adds a random song from the music directory to the queue
	 *
	 * @return successfully started playing
	 */
	public boolean playRandomSong() {
		return addToQueue(getRandomSong());
	}

	public boolean addToQueue(String filename) {
		File mp3file = new File(filename);
		if (!mp3file.exists()) {//check in config directory
			bot.out.sendErrorToMe(new Exception("NoMusicFile"), "filename: ", mp3file.getAbsolutePath(), "plz fix", "I want music", bot);
			return false;
		}
		LocalSource ls = new LocalSource(mp3file);
		player.getAudioQueue().add(ls);
		if (!player.isPlaying()) {
			player.play();
		}
		return true;
	}

	public float getVolume() {
		return player.getVolume();
	}

	public void setVolume(float volume) {
		volume = Math.min(1F, Math.max(0F, volume));
		player.setVolume(volume);
	}

	public List<User> getUsersInVoiceChannel() {
		ArrayList<User> userList = new ArrayList<>();
		VoiceChannel currentChannel = guild.getAudioManager().getConnectedChannel();
		if (currentChannel != null) {
			List<User> connectedUsers = currentChannel.getUsers();
			userList.addAll(connectedUsers.stream().filter(user -> !user.isBot()).collect(Collectors.toList()));
		}
		return userList;
	}

	public void stopMusic() {
		player.stop();
	}

	public List<OMusic> getQueue() {
		ArrayList<OMusic> list = new ArrayList<>();
		for (AudioSource audioSource : player.getAudioQueue()) {
			AudioInfo info = audioSource.getInfo();
		}
		return list;
	}
}
