package discordbot.handler;

import discordbot.db.WebDb;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.guildsettings.defaults.SettingMusicChannelTitle;
import discordbot.guildsettings.defaults.SettingMusicPlayingMessage;
import discordbot.guildsettings.defaults.SettingMusicVolume;
import discordbot.handler.audiosources.StreamSource;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.hooks.PlayerEventListener;
import net.dv8tion.jda.player.hooks.events.FinishEvent;
import net.dv8tion.jda.player.hooks.events.PlayEvent;
import net.dv8tion.jda.player.hooks.events.PlayerEvent;
import net.dv8tion.jda.player.hooks.events.SkipEvent;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.LocalSource;
import net.dv8tion.jda.utils.PermissionUtil;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MusicPlayerHandler {
	private final static Map<Guild, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
	private final Guild guild;
	private final DiscordBot bot;
	private final MusicPlayer player;
	private final PlayerEventHandler pvh;
	private volatile int currentlyPlaying = 0;
	private volatile long currentSongLength = 0;
	private volatile long currentSongStartTimeInSeconds = 0;
	private volatile int activePlayListId = 0;
	private Random rng;
	private AudioManager manager;
	private volatile LinkedList<OMusic> queue;

	private MusicPlayerHandler(Guild guild, DiscordBot bot) {
		queue = new LinkedList<>();
		pvh = new PlayerEventHandler();
		this.guild = guild;
		this.bot = bot;
		rng = new Random();
		manager = guild.getAudioManager();
		if (manager.getSendingHandler() == null) {
			manager = guild.getAudioManager();
			player = new MusicPlayer();
			manager.setSendingHandler(player);
			player.addEventListener(pvh);
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

	public int getActivePLaylistId() {
		return activePlayListId;
	}

	public synchronized void setActivePlayListId(int id) {
		activePlayListId = id;
	}

	public long getStartTimeStamp() {
		return currentSongStartTimeInSeconds;
	}

	private synchronized void trackEnded() throws InterruptedException {
		currentSongLength = 0;
		LinkedList<AudioSource> audioQueue = player.getAudioQueue();
		if (audioQueue.isEmpty()) {
			if (queue.isEmpty()) {
				audioQueue.add(new LocalSource(new File(getRandomSong())));
			} else {
				OMusic poll = queue.poll();
				audioQueue.add(new LocalSource(new File(poll.filename)));
			}
		}
	}

	private synchronized void trackStarted() throws IOException {
		currentSongStartTimeInSeconds = System.currentTimeMillis() / 1000L;
		OMusic record;
		File f = null;
		final String messageType = GuildSettings.get(guild).getOrDefault(SettingMusicPlayingMessage.class);
		AudioInfo info = player.getCurrentAudioSource().getInfo();
		if (info != null) {
			f = new File(info.getOrigin());
			record = TMusic.findByFileName(f.getAbsolutePath());
			if (record.id > 0) {
				record.lastplaydate = System.currentTimeMillis() / 1000L;
				TMusic.update(record);
				currentlyPlaying = record.id;
				currentSongLength = info.getDuration().getTotalSeconds();
			}
		} else {
			record = new OMusic();
		}
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingMusicChannelTitle.class))) {
			if (PermissionUtil.checkPermission(bot.getMusicChannel(guild), bot.client.getSelfInfo(), Permission.MANAGE_CHANNEL)) {
				bot.getMusicChannel(guild).getManager().setTopic(":notes: " + record.youtubeTitle).update();
			}
		}
		if (!messageType.equals("off") && record.id > 0) {
			String msg = "";
			if (activePlayListId == 0) {
//				msg = "[ no `" + DisUtil.getCommandPrefix(guild) + "playlist`] ";
			} else {
//				msg = "[some list] ";
			}
			if (record.youtubeTitle.isEmpty()) {
				msg += "plz send help:: " + f.getName();
			} else {
				if (record.artist != null && record.title != null && !record.artist.trim().isEmpty() && !record.title.trim().isEmpty()) {
					msg += ":notes: " + record.artist + " - " + record.title;
				} else {
					msg += ":notes: " + record.youtubeTitle;
				}
			}
			final long deleteAfter = currentSongLength * 1000L;
			bot.getMusicChannel(guild).sendMessageAsync(msg, message -> {
				if (messageType.equals("clear")) {
					bot.timer.schedule(
							new TimerTask() {
								@Override
								public void run() {
									message.deleteMessage();
								}
							}, deleteAfter
					);
				}
			});
		}
	}

	public boolean isConnectedTo(VoiceChannel channel) {
		return channel.equals(guild.getAudioManager().getConnectedChannel());
	}

	public void connectTo(VoiceChannel channel) {
		guild.getAudioManager().openAudioConnection(channel);
	}

	public boolean isConnected() {
		return guild.getAudioManager().getConnectedChannel() != null;
	}

	public boolean leave() {
		if (!isConnected()) {
			return false;
		}
		stopMusic();
		guild.getAudioManager().closeAudioConnection();
		return true;
	}

	public int getCurrentlyPlaying() {
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
	public synchronized void skipSong() {
		player.skipToNext();
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

	public synchronized void startPlaying() {
		if (!player.isPlaying()) {
			try {
				trackEnded();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			player.play();
			Launcher.log("Start playing", "music", "start",
					"guild-id", guild.getId(),
					"guild-name", guild.getName());
		}
	}

	public synchronized boolean addToQueue(String filename) {
		File mp3file = new File(filename);
		if (!mp3file.exists()) {//check in config directory
			bot.out.sendErrorToMe(new Exception("NoMusicFile"), "filename: ", mp3file.getAbsolutePath(), "plz fix", "I want music", bot);
			return false;
		}
		OMusic record = TMusic.findByFileName(mp3file.getAbsolutePath());
		if (record.id == 0) {
			bot.out.sendErrorToMe(new Exception("No record for file"), "filename: ", mp3file.getAbsolutePath(), "plz fix", "I want music", bot);
			return false;
		}
		queue.offer(record);
		startPlaying();
//		LocalSource ls = new LocalSource(mp3file);
//		player.getAudioQueue().add(ls);
//		if (!player.isPlaying()) {
//			player.play();
//		}
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

	public synchronized void stopMusic() {
		currentlyPlaying = 0;
		player.stop();
		Launcher.log("Stop playing", "music", "stop",
				"guild-id", guild.getId(),
				"guild-name", guild.getName());
	}

	public List<OMusic> getQueue() {
		return queue.stream().collect(Collectors.toList());
	}

	public synchronized void addStream(String url) {
		LinkedList<AudioSource> audioQueue = player.getAudioQueue();
		audioQueue.add(new StreamSource(url));
	}

	public synchronized void clearQueue() {
		queue.clear();
	}

	/**
	 * Handles events for the music player
	 */
	private class PlayerEventHandler implements PlayerEventListener {

		@Override
		public void onEvent(PlayerEvent event) {
			if (event instanceof SkipEvent || event instanceof FinishEvent) {
				try {
					trackEnded();
					if (!player.isPlaying()) {
						player.play();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (event instanceof PlayEvent) {
				try {
					trackStarted();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
