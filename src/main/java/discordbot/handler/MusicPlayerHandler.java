package discordbot.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discordbot.db.WebDb;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CMusic;
import discordbot.db.controllers.CMusicLog;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.model.OMusic;
import discordbot.db.model.OPlaylist;
import discordbot.guildsettings.music.*;
import discordbot.handler.audio.AudioPlayerSendHandler;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.VoiceStatus;
import net.dv8tion.jda.utils.PermissionUtil;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class MusicPlayerHandler {
	private final static DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	private final static Map<Guild, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
	private final Guild guild;
	private final DiscordBot bot;
	private final AudioPlayer player;
	private final TrackScheduler scheduler;

	private volatile boolean inRepeatMode = false;

	private volatile int currentlyPlaying = 0;
	private volatile long currentSongLength = 0;
	private volatile long pauseStart = 0;
	private volatile boolean updateChannelTitle = false;
	private volatile long currentSongStartTimeInSeconds = 0;
	private volatile int activePlayListId = 0;
	private volatile OPlaylist playlist;
	private Random rng;
	private volatile LinkedList<OMusic> queue;

	private MusicPlayerHandler(Guild guild, DiscordBot bot) {
		queue = new LinkedList<>();
		this.guild = guild;
		this.bot = bot;
		rng = new Random();
		player = playerManager.createPlayer();
		scheduler = new TrackScheduler(player);
		player.addListener(scheduler);
		guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
		player.setVolume(Integer.parseInt(GuildSettings.get(guild).getOrDefault(SettingMusicVolume.class)));
		playerInstances.put(guild, this);
		int savedPlaylist = Integer.parseInt(GuildSettings.get(guild).getOrDefault(SettingMusicLastPlaylist.class));
		if (savedPlaylist > 0) {
			playlist = CPlaylist.findById(savedPlaylist);
		}
		if (savedPlaylist == 0 || playlist.id == 0) {
			playlist = CPlaylist.getGlobalList();
		}
		activePlayListId = playlist.id;
	}

	public static void init() {
		AudioSourceManagers.registerLocalSource(playerManager);
		playerManager.setFrameBufferDuration(10);
		playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
	}

	public static void removeGuild(Guild guild) {
		if (playerInstances.containsKey(guild)) {
			playerInstances.remove(guild);
		}
	}

	public static MusicPlayerHandler getFor(Guild guild, DiscordBot bot) {
		if (playerInstances.containsKey(guild)) {
			return playerInstances.get(guild);
		} else {
			return new MusicPlayerHandler(guild, bot);
		}
	}

	/**
	 * Check if a user meets the requirements to use the music commands
	 *
	 * @return bool
	 */
	public boolean canUseVoiceCommands(User user, SimpleRank rank) {
		if (PermissionUtil.checkPermission(guild, user, Permission.ADMINISTRATOR)) {
			return true;
		}
		if (!GuildSettings.get(guild).canUseMusicCommands(user, rank)) {
			return false;
		}
		VoiceStatus voiceStatus = guild.getVoiceStatusOfUser(user);
		if (voiceStatus == null) {
			return false;
		}
		VoiceChannel userVoice = voiceStatus.getChannel();
		if (userVoice == null) {
			return false;
		}
		if (guild.getAudioManager().getConnectedChannel() != null) {
			if (!guild.getAudioManager().getConnectedChannel().equals(userVoice)) {
				return false;
			}
		}
		return true;
	}

	public synchronized boolean isInRepeatMode() {
		return inRepeatMode;
	}

	public synchronized void setRepeat(boolean repeatMode) {
		inRepeatMode = repeatMode;
	}

	public int getActivePLaylistId() {
		return activePlayListId;
	}

	/**
	 * Sets the active playlist, or refreshes the currently cached one
	 *
	 * @param id internal id of the playlist
	 */
	public synchronized void setActivePlayListId(int id) {
		playlist = CPlaylist.findById(id);
		if (activePlayListId != playlist.id) {
			activePlayListId = playlist.id;
			GuildSettings.get(guild).set(SettingMusicLastPlaylist.class, "" + id);
		}
	}

	private synchronized void trackEnded() {
		currentSongLength = 0;
		boolean keepGoing = false;
		if (scheduler.queue.isEmpty()) {
			if (queue.isEmpty()) {
				if ("false".equals(GuildSettings.get(guild).getOrDefault(SettingMusicQueueOnly.class))) {
					String filename = getRandomSong();
					keepGoing = true;
					if (filename != null) {
						addToQueue(filename, null);
					} else {
						player.destroy();
						bot.getMusicChannel(guild).sendMessageAsync("Stopped playing because the playlist is empty", null);
						return;
					}
				} else {
					leave();
				}
			}
			final OMusic trackToAdd = queue.poll();
			if (trackToAdd == null) {
				return;
			}
			String absolutePath = new File(trackToAdd.filename).getAbsolutePath();
			boolean finalKeepGoing = keepGoing;
			playerManager.loadItemOrdered(player, absolutePath, new AudioLoadResultHandler() {
				@Override
				public void trackLoaded(AudioTrack track) {
					scheduler.queue(track);
					startPlaying();
				}

				@Override
				public void playlistLoaded(AudioPlaylist playlist) {
				}

				@Override
				public void noMatches() {
				}

				@Override
				public void loadFailed(FriendlyException exception) {
					bot.out.sendMessageToCreator("file:" + absolutePath);
					trackToAdd.banned = 1; //keep them from the global playlist
					CMusic.update(trackToAdd);
					new File(absolutePath).delete();
					if (finalKeepGoing) {
						trackEnded();
					}
				}
			});
		}
	}

	private synchronized void trackStarted() throws IOException {
		if (currentlyPlaying != 0 && pauseStart > 0) {
			pauseStart = 0;
			return;
		}
		currentSongStartTimeInSeconds = System.currentTimeMillis() / 1000L;
		OMusic record;
		File f = null;
		final String messageType = GuildSettings.get(guild).getOrDefault(SettingMusicPlayingMessage.class);
		AudioTrackInfo info = player.getPlayingTrack().getInfo();
		if (info != null) {
			f = new File(info.identifier);
			record = CMusic.findByFileName(f.getAbsolutePath());
			if (record.id > 0) {
				record.lastplaydate = System.currentTimeMillis() / 1000L;
				CMusic.update(record);
				currentlyPlaying = record.id;
				currentSongLength = info.length / 1000L;
				CMusicLog.insert(CGuild.getCachedId(guild.getId()), record.id, 0);
				if (!playlist.isGlobalList()) {
					CPlaylist.updateLastPlayed(playlist.id, record.id);
				}
			}
		} else {
			record = new OMusic();
		}
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingMusicChannelTitle.class))) {
			if (bot.getMusicChannel(guild) != null && bot.getMusicChannel(guild).checkPermission(bot.client.getSelfInfo(), Permission.MANAGE_CHANNEL)) {
				if (!isUpdateChannelTitle()) {
					bot.getMusicChannel(guild).getManager().setTopic("\uD83C\uDFB6 " + record.youtubeTitle).update();
				}
			} else {
				GuildSettings.get(guild).set(SettingMusicChannelTitle.class, "false");
			}
		}
		if (!messageType.equals("off") && record.id > 0) {
			if (!bot.getMusicChannel(guild).checkPermission(bot.client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
				GuildSettings.get(guild).set(SettingMusicPlayingMessage.class, "off");
				return;
			}
			String msg = "[`" + DisUtil.getCommandPrefix(guild) + "pl` " + playlist.title + "] ";
			if (record.artist != null && record.title != null && !record.artist.trim().isEmpty() && !record.title.trim().isEmpty()) {
				msg += "\uD83C\uDFB6 " + record.artist + " - " + record.title;
			} else {
				msg += "\uD83C\uDFB6 " + record.youtubeTitle;
			}
			final long deleteAfter = currentSongLength * 1000L;
			bot.getMusicChannel(guild).sendMessageAsync(msg, message -> {
				if (messageType.equals("clear")) {
					bot.timer.schedule(
							new TimerTask() {
								@Override
								public void run() {
									if (message != null) {
										message.deleteMessage();
									}
								}
							}, Math.max(deleteAfter, 60_000L)
					);
				}
			});
		}
	}

	public boolean isConnectedTo(VoiceChannel channel) {
		return channel.equals(guild.getAudioManager().getConnectedChannel());
	}

	public synchronized void connectTo(VoiceChannel channel) {
		if (!isConnectedTo(channel)) {
			if (guild.getAudioManager().getQueuedAudioConnection() == null) {
				guild.getAudioManager().openAudioConnection(channel);
			}
		}
	}

	public boolean isConnected() {
		return guild.getAudioManager().getConnectedChannel() != null;
	}

	public boolean leave() {
		if (isConnected()) {
			stopMusic();
		}
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
		if (!player.isPaused()) {
			return currentSongStartTimeInSeconds;
		}
		return currentSongStartTimeInSeconds + (System.currentTimeMillis() / 1000L - pauseStart);
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
		scheduler.skipTrack();
	}

	/**
	 * retreives a random .mp3 file from the music directory
	 *
	 * @return filename
	 */
	private String getRandomSong() {
		ArrayList<String> potentialSongs = new ArrayList<>();
		if (!playlist.isGlobalList()) {
			return CPlaylist.getNextTrack(playlist.id, playlist.getPlayType());
		}
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
	public synchronized boolean playRandomSong() {
		String randomSong = getRandomSong();
		if (randomSong != null) {
			return addToQueue(randomSong, null);
		}
		return false;
	}

	public synchronized boolean isPlaying() {
		return player.getPlayingTrack() != null;
	}

	public synchronized void startPlaying() {
		if (!isPlaying()) {
			if (player.isPaused()) {
				player.setPaused(false);
			} else {
				scheduler.skipTrack();
			}
			Launcher.log("Start playing", "music", "start",
					"guild-id", guild.getId(),
					"guild-name", guild.getName());
		}
	}

	public synchronized boolean addToQueue(String filename, User user) {
		File mp3file = new File(filename);
		if (!mp3file.exists()) {//check in config directory
			bot.out.sendErrorToMe(new Exception("NoMusicFile"), "filename: ", mp3file.getAbsolutePath(), "plz fix", "I want music", bot);
			return false;
		}
		OMusic record = CMusic.findByFileName(mp3file.getAbsolutePath());
		if (record.id == 0) {
			bot.out.sendErrorToMe(new Exception("No record for file"), "filename: ", mp3file.getAbsolutePath(), "plz fix", "I want music", bot);
			return false;
		}
		if (!playlist.isGlobalList() && user != null) {
			if (playlist.isGuildList() && guild.isMember(user)) {
				switch (playlist.getEditType()) {
					case PRIVATE_AUTO:
						if (!PermissionUtil.checkPermission(guild, user, Permission.ADMINISTRATOR)) {
							break;
						}
					case PUBLIC_AUTO:
						CPlaylist.addToPlayList(playlist.id, record.id);
					default:
						break;
				}
			}
		}
		queue.offer(record);
		startPlaying();
		return true;
	}

	public int getVolume() {
		return player.getVolume();
	}

	public void setVolume(int volume) {
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

	/**
	 * check if the player can be paused to start with
	 *
	 * @return if its either playing or already paused
	 */
	public synchronized boolean canTogglePause() {
		return player.getPlayingTrack() != null || player.isPaused();
	}

	/**
	 * toggle paused
	 *
	 * @return true if paused, false otherwise
	 */
	public synchronized boolean togglePause() {
		if (!player.isPaused()) {
			pauseStart = System.currentTimeMillis() / 1000L;
			player.setPaused(true);
		} else {
			currentSongStartTimeInSeconds += (System.currentTimeMillis() / 1000L) - pauseStart;
			player.setPaused(false);
		}
		return player.isPaused();
	}

	public synchronized boolean isPaused() {
		return player.isPaused();
	}

	public synchronized void stopMusic() {
		currentlyPlaying = 0;
		player.destroy();
		Launcher.log("Stop playing", "music", "stop",
				"guild-id", guild.getId(),
				"guild-name", guild.getName());
	}

	public List<OMusic> getQueue() {
		return queue.stream().collect(Collectors.toList());
	}

	public synchronized void addStream(String url) {

	}

	public synchronized void clearQueue() {
		queue.clear();
	}

	public boolean isUpdateChannelTitle() {
		return updateChannelTitle;
	}

	public void setUpdateChannelTitle(boolean updateChannelTitle) {
		this.updateChannelTitle = updateChannelTitle;
	}


	public class TrackScheduler extends AudioEventAdapter {
		private final AudioPlayer player;
		private final BlockingQueue<AudioTrack> queue;

		public TrackScheduler(AudioPlayer player) {
			this.player = player;
			this.queue = new LinkedBlockingQueue<>();
		}

		public void queue(AudioTrack track) {
			if (!player.startTrack(track, true)) {
				queue.offer(track);
			}
		}

		@Override
		public void onTrackStart(AudioPlayer player, AudioTrack track) {
			try {
				trackStarted();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void skipTrack() {
			trackEnded();//yes it ended, go away
			if (isInRepeatMode() && player.getPlayingTrack() != null) {
				player.startTrack(player.getPlayingTrack().makeClone(), false);
				return;
			}
			player.stopTrack();
			AudioTrack poll = queue.poll();
			if (poll != null) {
				player.startTrack(poll, false);
			}
		}

		@Override
		public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
			if (endReason.mayStartNext) {
				if (isInRepeatMode()) {
					player.startTrack(track.makeClone(), false);
					return;
				}
				skipTrack();
			}
		}
	}
}
