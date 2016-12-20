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
import discordbot.guildsettings.music.SettingMusicChannelTitle;
import discordbot.guildsettings.music.SettingMusicLastPlaylist;
import discordbot.guildsettings.music.SettingMusicPlayingMessage;
import discordbot.guildsettings.music.SettingMusicQueueOnly;
import discordbot.guildsettings.music.SettingMusicVolume;
import discordbot.guildsettings.music.SettingMusicVotePercent;
import discordbot.handler.audio.AudioPlayerSendHandler;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.permission.SimpleRank;
import discordbot.util.Emojibet;
import discordbot.util.MusicUtil;
import discordbot.util.YTUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MusicPlayerHandler {
	private final static DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	private final static Map<String, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
	private final DiscordBot bot;
	private final AudioPlayer player;
	private final TrackScheduler scheduler;
	private final HashSet<User> skipVotes;
	private final String guildId;
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

	private MusicPlayerHandler(String guild, DiscordBot bot) {
		queue = new LinkedList<>();
		this.bot = bot;
		this.guildId = guild;
		rng = new Random();
		player = playerManager.createPlayer();
		scheduler = new TrackScheduler(player);
		player.addListener(scheduler);
		player.setVolume(Integer.parseInt(GuildSettings.get(guild).getOrDefault(SettingMusicVolume.class)));
		bot.client.getGuildById(guild).getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
		playerInstances.put(guild, this);
		int savedPlaylist = Integer.parseInt(GuildSettings.get(guild).getOrDefault(SettingMusicLastPlaylist.class));
		if (savedPlaylist > 0) {
			playlist = CPlaylist.findById(savedPlaylist);
		}
		if (savedPlaylist == 0 || playlist.id == 0) {
			playlist = CPlaylist.getGlobalList();
		}
		activePlayListId = playlist.id;
		skipVotes = new HashSet<>();
	}

	public static void init() {
		AudioSourceManagers.registerLocalSource(playerManager);
		playerManager.setFrameBufferDuration(10);
		playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
	}

	public static void removeGuild(Guild guild) {
		if (playerInstances.containsKey(guild.getId())) {
			playerInstances.get(guild.getId()).leave();
			playerInstances.remove(guild.getId());
		}
	}

	public static MusicPlayerHandler getFor(Guild guild, DiscordBot bot) {
		if (playerInstances.containsKey(guild.getId())) {
			return playerInstances.get(guild.getId());
		} else {
			return new MusicPlayerHandler(guild.getId(), bot);
		}
	}

	public OPlaylist getPlaylist() {
		return playlist;
	}

	public String getGuild() {
		return guildId;
	}

	public boolean isInVoiceWith(Guild guild, User author) {
		VoiceChannel channel = guild.getMember(author).getVoiceState().getChannel();
		if (channel == null) {
			return false;
		}
		for (Member user : channel.getMembers()) {
			if (user.getUser().getId().equals(guild.getJDA().getSelfUser().getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a user meets the requirements to use the music commands
	 *
	 * @return bool
	 */
	public boolean canUseVoiceCommands(User user, SimpleRank rank) {
		Guild guild = user.getJDA().getGuildById(guildId);
		if (PermissionUtil.checkPermission(guild, guild.getMember(user), Permission.ADMINISTRATOR)) {
			return true;
		}
		if (!GuildSettings.get(guild).canUseMusicCommands(user, rank)) {
			return false;
		}
		GuildVoiceState voiceStatus = guild.getMember(user).getVoiceState();
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
			GuildSettings.get(guildId).set(SettingMusicLastPlaylist.class, "" + id);
		}
	}

	private synchronized void trackEnded() {
		currentSongLength = 0;
		boolean keepGoing = false;
		if (scheduler.queue.isEmpty()) {
			if (queue.isEmpty()) {
				if ("false".equals(GuildSettings.get(guildId).getOrDefault(SettingMusicQueueOnly.class))) {
					keepGoing = true;
					if (!playRandomSong()) {
						player.destroy();
						bot.getMusicChannel(guildId).sendMessage("Stopped playing because the playlist is empty").queue();
						leave();
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
					bot.out.sendMessageToCreator("file:" + absolutePath + Config.EOL + "Message: " + exception.getMessage());
					trackToAdd.fileExists = 0;
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
		skipVotes.clear();
		currentSongStartTimeInSeconds = System.currentTimeMillis() / 1000L;
		OMusic record;
		final String messageType = GuildSettings.get(guildId).getOrDefault(SettingMusicPlayingMessage.class);
		AudioTrackInfo info = player.getPlayingTrack().getInfo();
		if (info != null) {
			File f = new File(info.identifier);
			record = CMusic.findByFileName(f.getAbsolutePath());
			if (record.id > 0) {
				if (record.duration == 0) {
					YTUtil.getTrackDuration(record);
				}
				record.lastplaydate = System.currentTimeMillis() / 1000L;
				CMusic.update(record);
				currentlyPlaying = record.id;
				currentSongLength = record.duration;
				CMusicLog.insert(CGuild.getCachedId(guildId), record.id, 0);
				if (!playlist.isGlobalList()) {
					CPlaylist.updateLastPlayed(playlist.id, record.id);
				}
			}
		} else {
			record = new OMusic();
		}
		if ("true".equals(GuildSettings.get(guildId).getOrDefault(SettingMusicChannelTitle.class))) {
			Guild guild = bot.client.getGuildById(guildId);
			if (bot.getMusicChannel(guildId) != null && PermissionUtil.checkPermission(bot.getMusicChannel(guildId), guild.getSelfMember(), Permission.MANAGE_CHANNEL)) {
				if (!isUpdateChannelTitle()) {
					bot.getMusicChannel(guildId).getManager().setTopic("\uD83C\uDFB6 " + record.youtubeTitle).queue();
				}
			} else {
				GuildSettings.get(guildId).set(SettingMusicChannelTitle.class, "false");
			}
		}
		if (!messageType.equals("off") && record.id > 0) {
			if (!bot.getMusicChannel(guildId).canTalk()) {
				GuildSettings.get(guildId).set(SettingMusicPlayingMessage.class, "off");
				return;
			}
			final long deleteAfter = Math.min(Math.max(currentSongLength * 1000L, 60_000L), 7200_000L);
			Consumer<Message> callback = (message) -> {
				if (messageType.equals("clear")) {
					bot.schedule(() -> {
								if (message != null) {
									message.deleteMessage().queue();
								}
							}
							, deleteAfter, TimeUnit.MILLISECONDS);
				}
				bot.musicReactionHandler.clearGuild(guildId);
				Guild guild = bot.client.getGuildById(guildId);
				if (PermissionUtil.checkPermission(message.getTextChannel(), guild.getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
					message.addReaction(Emojibet.NEXT_TRACK).queue();
					if (aListenerIsAtLeast(SimpleRank.BOT_ADMIN)) {
						message.addReaction(Emojibet.NO_ENTRY).queue();
					}
					bot.musicReactionHandler.addMessage(guildId, message.getId());
				}
			};
			Guild guild = bot.client.getGuildById(guildId);
			if (!PermissionUtil.checkPermission(bot.getMusicChannel(guildId), guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
				bot.getMusicChannel(guildId).sendMessage(MusicUtil.nowPlayingMessageNoEmbed(this, record)).queue(callback);
			} else {
				bot.getMusicChannel(guildId).sendMessage(MusicUtil.nowPlayingMessage(this, record)).queue(callback);
			}
		}
	}

	public boolean isConnectedTo(VoiceChannel channel) {
		return channel != null && channel.equals(channel.getJDA().getGuildById(guildId).getAudioManager().getConnectedChannel());
	}

	public synchronized void connectTo(VoiceChannel channel) {
		if (!isConnectedTo(channel)) {
			Guild guild = channel.getJDA().getGuildById(guildId);
			if (!guild.getAudioManager().isConnected()) {
				guild.getAudioManager().setSelfDeafened(true);
			}
			guild.getAudioManager().openAudioConnection(channel);
		}
	}

	public boolean isConnected() {
		Guild guildById = bot.client.getGuildById(guildId);
		return guildById != null && guildById.getAudioManager().getConnectedChannel() != null;
	}

	public boolean leave() {
		if (isConnected()) {
			stopMusic();
		}
		Guild guild = bot.client.getGuildById(guildId);
		if (guild != null) {
			guild.getAudioManager().closeAudioConnection();
		}
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

	public synchronized void unregisterVoteSkip(User user) {
		skipVotes.remove(user);
	}

	public synchronized boolean voteSkip(User user) {
		if (skipVotes.contains(user)) {
			return false;
		}
		skipVotes.add(user);
		return true;
	}

	/**
	 * retrieves the amount skip votes
	 *
	 * @return votes
	 */
	public synchronized int getVoteCount() {
		return skipVotes.size();
	}

	/**
	 * Retrieves the amount of required votes in order to skip the track
	 *
	 * @return required votes
	 */
	public synchronized int getRequiredVotes() {
		return Math.max(1, (int) (Double.parseDouble(GuildSettings.get(guildId).getOrDefault(SettingMusicVotePercent.class)) / 100D * (double) getUsersInVoiceChannel().size()));
	}

	/**
	 * Forcefully Skips the currently playing song
	 */
	public synchronized void forceSkip() {
		scheduler.skipTrack();
	}

	/**
	 * retreives a random file from the music directory
	 *
	 * @return filename OR null when the music table is empty
	 */
	private String getRandomSong() {
		ArrayList<String> potentialSongs = new ArrayList<>();
		if (!playlist.isGlobalList()) {
			return CPlaylist.getNextTrack(playlist.id, playlist.getPlayType());
		}
		try (ResultSet rs = WebDb.get().select(
				"SELECT filename, youtube_title, lastplaydate " +
						"FROM music " +
						"WHERE banned = 0 AND file_exists = 1 " +
						"ORDER BY lastplaydate ASC " +
						"LIMIT 50")) {
			while (rs.next()) {
				potentialSongs.add(rs.getString("filename"));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			e.printStackTrace();
			bot.getContainer().reportError(e);
		}
		if (potentialSongs.isEmpty()) {
			return null;
		}
		return potentialSongs.get(rng.nextInt(potentialSongs.size()));
	}

	/**
	 * Adds a random song from the music directory to the queue
	 * if a track fails, try the next one
	 *
	 * @return successfully started playing
	 */
	public synchronized boolean playRandomSong() {
		while (true) {
			String randomSong = getRandomSong();
			if (randomSong != null) {
				if (addToQueue(randomSong, null)) {
					return true;
				}
			} else return false;
		}
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
					"guild-id", guildId);
		}
	}

	public synchronized boolean addToQueue(String filename, User user) {
		File musicFile = new File(filename);
		OMusic record = CMusic.findByYoutubeId(musicFile.getName().substring(0, musicFile.getName().lastIndexOf(".")));
		if (record.id == 0) {
			bot.getContainer().reportError(new Exception("No record for file"), "filename: ", musicFile.getAbsolutePath(), "plz fix", "I want music", bot);
			return false;
		}
		if (!musicFile.exists()) {//check in config directory
			record.fileExists = 0;
			CMusic.update(record);
			bot.getContainer().reportError(new Exception("NoMusicFile"), "filename: ", musicFile.getAbsolutePath(), "plz fix", "I want music", bot);
			return false;
		}
		if (!record.filename.equals(musicFile.getAbsolutePath())) {
			record.filename = musicFile.getAbsolutePath();
			CMusic.update(record);
		}
		if (!playlist.isGlobalList() && user != null) {
			Guild guild = user.getJDA().getGuildById(guildId);
			if (playlist.isGuildList() && guild.isMember(user)) {
				switch (playlist.getEditType()) {
					case PRIVATE_AUTO:
						if (!PermissionUtil.checkPermission(guild, guild.getMember(user), Permission.ADMINISTRATOR)) {
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

	public List<Member> getUsersInVoiceChannel() {
		ArrayList<Member> userList = new ArrayList<>();
		VoiceChannel currentChannel = bot.client.getGuildById(guildId).getAudioManager().getConnectedChannel();
		if (currentChannel != null) {
			List<Member> connectedUsers = currentChannel.getMembers();
			userList.addAll(connectedUsers.stream().filter(user -> !user.getUser().isBot() && !user.getVoiceState().isDeafened()).collect(Collectors.toList()));
		}
		return userList;
	}

	/**
	 * Is there a listener of at least this rank?
	 *
	 * @param rank the rank to be
	 * @return found a user?
	 */
	public boolean aListenerIsAtLeast(SimpleRank rank) {
		VoiceChannel currentChannel = bot.client.getGuildById(guildId).getAudioManager().getConnectedChannel();
		if (currentChannel != null) {
			for (Member member : currentChannel.getMembers()) {
				if (member.getVoiceState().isDeafened() || member.getUser().isBot()) {
					continue;
				}
				if (bot.security.getSimpleRank(member.getUser()).isAtLeast(rank)) {
					return true;
				}
			}
		}
		return false;
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
				"guild-id", guildId);
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
