/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.handler;

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
import emily.db.WebDb;
import emily.db.controllers.*;
import emily.db.model.OMusic;
import emily.db.model.OPlaylist;
import emily.guildsettings.GSetting;
import emily.handler.audio.AudioPlayerSendHandler;
import emily.handler.audio.QueuedAudioTrack;
import emily.handler.discord.MessageMetaData;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.permission.SimpleRank;
import emily.util.Emojibet;
import emily.util.MusicUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MusicPlayerHandler {
    private final static DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final static Map<Long, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
    public final AudioPlayer player;
    private final DiscordBot bot;
    private final TrackScheduler scheduler;
    private final HashSet<User> skipVotes;
    private final long guildId;
    private volatile boolean inRepeatMode = false;
    private volatile boolean stopAfterTrack = false;
    private volatile int currentlyPlaying = 0;
    private volatile long currentSongLength = 0;
    private volatile long pauseStart = 0;
    private volatile boolean updateChannelTitle = false;
    private volatile long currentSongStartTimeInSeconds = 0;
    private volatile int activePlayListId;
    private volatile OPlaylist playlist;
    private Random rng;
    private volatile LinkedList<OMusic> queue;
    private final ArrayList<MessageMetaData> messagesToDelete = new ArrayList<>();

    /**
     * Will delete the message when the currently playing track ends
     *
     * @param channelId the channel the message is put in
     * @param messageId the id of the message
     */
    public void deleteMessageAfterTrack(long channelId, long messageId) {
        messagesToDelete.add(new MessageMetaData(channelId, messageId));
    }

    private MusicPlayerHandler(Guild guild, DiscordBot bot) {

        rng = new Random();
        AudioManager guildManager = guild.getAudioManager();
        player = playerManager.createPlayer();
        this.bot = bot;
        this.guildId = guild.getIdLong();
        guildManager.setSendingHandler(new AudioPlayerSendHandler(player));
        queue = new LinkedList<>();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        player.setVolume(Integer.parseInt(GuildSettings.get(guild.getIdLong()).getOrDefault(GSetting.MUSIC_VOLUME)));
        playerInstances.put(guild.getIdLong(), this);
        int savedPlaylist = Integer.parseInt(GuildSettings.get(guild.getIdLong()).getOrDefault(GSetting.MUSIC_PLAYLIST_ID));
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
        AudioSourceManagers.registerRemoteSources(playerManager);
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
    }

    public static void removeGuild(Guild guild) {
        removeGuild(guild, false);
    }

    public static void removeGuild(Guild guild, boolean saveStatus) {
        if (playerInstances.containsKey(guild.getIdLong())) {
            if (saveStatus && playerInstances.get(guild.getIdLong()).isConnected()) {
                CBotPlayingOn.insert(guild.getId(), guild.getAudioManager().getConnectedChannel().getId());
            }
            playerInstances.get(guild.getIdLong()).leave();
            playerInstances.remove(guild.getIdLong());
        }
    }

    public static MusicPlayerHandler getFor(Guild guild) {
        return playerInstances.get(guild.getIdLong());
    }

    public static MusicPlayerHandler getFor(Guild guild, DiscordBot bot) {
        if (playerInstances.containsKey(guild.getIdLong())) {
            return playerInstances.get(guild.getIdLong());
        } else {
            return new MusicPlayerHandler(guild, bot);
        }
    }

    public void goToTime(Long millis) {
        player.getPlayingTrack().setPosition(millis);
    }

    public OPlaylist getPlaylist() {
        return playlist;
    }

    public long getGuild() {
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
        if (PermissionUtil.checkPermission(guild.getMember(user), Permission.ADMINISTRATOR)) {
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
            return guild.getAudioManager().getConnectedChannel().equals(userVoice);
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
            GuildSettings.get(guildId).set(null, GSetting.MUSIC_PLAYLIST_ID, "" + id);
        }
    }

    private synchronized void trackEnded() {
        currentSongLength = 0;
        boolean keepGoing = false;
        if (!messagesToDelete.isEmpty()) {
            for (MessageMetaData messageMetaData : messagesToDelete) {
                TextChannel chan = getJDA().getTextChannelById(messageMetaData.getChannelId());
                if (chan != null) {
                    chan.deleteMessageById(messageMetaData.getMessageId()).queue();
                }
            }
            messagesToDelete.clear();
        }

        if (scheduler.queue.isEmpty()) {
            if (queue.isEmpty()) {
                if (!stopAfterTrack && !GuildSettings.get(guildId).getBoolValue(GSetting.MUSIC_QUEUE_ONLY)) {
                    keepGoing = true;
                    if (!playRandomSong()) {
                        player.destroy();
                        bot.queue.add(bot.getMusicChannel(guildId).sendMessage("Stopped playing because the playlist is empty"));
                        bot.schedule(() -> MusicPlayerHandler.removeGuild(bot.getJda().getGuildById(guildId)), 10L, TimeUnit.SECONDS);
                        return;
                    }
                } else {
                    stopAfterTrack = false;
                    bot.schedule(() -> MusicPlayerHandler.removeGuild(bot.getJda().getGuildById(guildId)), 10L, TimeUnit.SECONDS);
                    return;
                }
            }
            final OMusic trackToAdd = queue.poll();
            if (trackToAdd == null) {
                return;
            }
            boolean finalKeepGoing = keepGoing;
            playerManager.loadItemOrdered(player, trackToAdd.youtubecode, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    if (track.getSourceManager().getSourceName().equals("youtube")) {
                        OMusic rec = CMusic.findByYoutubeId(track.getInfo().identifier);
                        if (rec.id == 0 || rec.duration == 0) {
                            rec.artist = track.getInfo().author;
                            rec.duration = (int) (track.getInfo().length / 1000L);
                            rec.youtubeTitle = track.getInfo().title;
                            CMusic.update(rec);
                        }
                    }
                    scheduler.queue(new QueuedAudioTrack(trackToAdd.requestedBy, track));
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
                    TextChannel musicChannel = bot.getMusicChannel(guildId);
                    if (musicChannel != null) {
                        bot.queue.add(musicChannel.sendMessage(String.format("can't play `%s`. Reason: %s", trackToAdd.youtubecode, exception.getMessage())));
                    }
                    if (finalKeepGoing) {
                        trackEnded();
                    }
                }
            });
        }
    }

    private synchronized void trackStarted() {
        if (currentlyPlaying != 0 && pauseStart > 0) {
            pauseStart = 0;
            return;
        }
        skipVotes.clear();
        currentSongStartTimeInSeconds = System.currentTimeMillis() / 1000L;
        OMusic record;
        final String messageType = GuildSettings.get(guildId).getOrDefault(GSetting.MUSIC_PLAYING_MESSAGE);
        AudioTrackInfo info = player.getPlayingTrack().getInfo();
        if (info != null) {
            record = CMusic.findByYoutubeId(info.identifier);
            if (record.id > 0) {
                if (((scheduler.getLastRequester() != null) && !scheduler.getLastRequester().isEmpty()) || !playlist.isGlobalList()) {
                    if (scheduler.getLastRequester() != null && !scheduler.getLastRequester().isEmpty()) {
                        record.playCount++;
                    }
                    record.lastplaydate = System.currentTimeMillis() / 1000L;
                    CMusic.update(record);
                }
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
        TextChannel musicChannel = bot.getMusicChannel(guildId);
        if ("true".equals(GuildSettings.get(guildId).getOrDefault(GSetting.MUSIC_CHANNEL_TITLE))) {
            Guild guild = bot.getJda().getGuildById(guildId);
            if (musicChannel != null && PermissionUtil.checkPermission(musicChannel, guild.getSelfMember(), Permission.MANAGE_CHANNEL)) {
                if (!isUpdateChannelTitle()) {
                    bot.queue.add(musicChannel.getManager().setTopic("\uD83C\uDFB6 " + record.youtubeTitle));
                }
            }
        }
        if (!"off".equals(messageType) && record.id > 0) {
            if (musicChannel == null || !musicChannel.canTalk()) {
                return;
            }
            Consumer<Message> callback = (message) -> {
                if (messageType.equals("clear")) {
                    deleteMessageAfterTrack(message.getChannel().getIdLong(), message.getIdLong());
                }
                bot.musicReactionHandler.clearGuild(guildId);
                Guild guild = bot.getJda().getGuildById(guildId);
                if (PermissionUtil.checkPermission(message.getTextChannel(), guild.getSelfMember(), Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_HISTORY)) {
                    message.addReaction(Emojibet.STAR).complete();
                    message.addReaction(Emojibet.NEXT_TRACK).complete();
                    if (aListenerIsAtLeast(SimpleRank.BOT_ADMIN)) {
                        message.addReaction(Emojibet.NO_ENTRY).complete();
                    }
                    bot.musicReactionHandler.addMessage(guildId, message.getIdLong());
                }
            };
            Guild guild = bot.getJda().getGuildById(guildId);
            if (!PermissionUtil.checkPermission(musicChannel, guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
                bot.queue.add(musicChannel.sendMessage(MusicUtil.nowPlayingMessageNoEmbed(this, record)), callback);
            } else {
                Member member = null;
                if (scheduler.getLastRequester() != null && !scheduler.getLastRequester().isEmpty()) {
                    member = guild.getMemberById(scheduler.getLastRequester());
                }
                bot.queue.add(musicChannel.sendMessage(MusicUtil.nowPlayingMessage(this, record, member)), callback);
            }
        }
    }

    public boolean isConnectedTo(VoiceChannel channel) {
        return channel != null && channel.equals(channel.getJDA().getGuildById(guildId).getAudioManager().getConnectedChannel());
    }

    public synchronized void connectTo(VoiceChannel channel) {
        if (channel != null && !isConnectedTo(channel)) {
            Guild guild = channel.getJDA().getGuildById(guildId);
            guild.getAudioManager().openAudioConnection(channel);
        }
    }

    public boolean isConnected() {
        Guild guildById = bot.getJda().getGuildById(guildId);
        return guildById != null && guildById.getAudioManager().getConnectedChannel() != null;
    }

    public boolean leave() {
        if (isConnected()) {
            stopMusic();
        }
        Guild guild = bot.getJda().getGuildById(guildId);
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
        return Math.max(1, (int) (Double.parseDouble(GuildSettings.get(guildId).getOrDefault(GSetting.MUSIC_VOTE_PERCENT)) / 100D * (double) getUsersInVoiceChannel().size()));
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
                "SELECT filename, youtube_title, lastplaydate, youtubecode " +
                        "FROM music " +
                        "WHERE banned = 0 " +
                        "AND play_count > 25 " +
                        "ORDER BY lastplaydate ASC " +
                        "LIMIT 50")) {
            while (rs.next()) {
                potentialSongs.add(rs.getString("youtubecode"));
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
     * Adds a random song from the music directory to the add
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
        OMusic record = CMusic.findByYoutubeId(filename);
        if (record.id == 0) {
            record.youtubecode = filename;
            CMusic.insert(record);
        }
        if (user != null) {
            record.requestedBy = user.getId();
        }
        if (!playlist.isGlobalList() && user != null) {
            Guild guild = user.getJDA().getGuildById(guildId);
            if (playlist.isGuildList() && guild.isMember(user)) {
                switch (playlist.getEditType()) {
                    case PRIVATE_AUTO:
                        if (!PermissionUtil.checkPermission(guild.getMember(user), Permission.ADMINISTRATOR)) {
                            break;
                        }
                    case PUBLIC_AUTO:
                        CPlaylist.addToPlayList(playlist.id, record.id);
                    default:
                        break;
                }
            } else if (playlist.isPersonal()) {
                switch (playlist.getEditType()) {
                    case PRIVATE_AUTO:
                        if (playlist.ownerId != CUser.getCachedId(user.getIdLong())) {
                            break;
                        }
                    case PUBLIC_AUTO:
                        CPlaylist.addToPlayList(playlist.id, record.id);
                        break;
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

    /**
     * retrieves a list of users who can listen and use voice commands
     * generating images is easy to make messy
     * for now
     *
     * @return list of users
     */
    public List<Member> getUsersInVoiceChannel() {
        ArrayList<Member> userList = new ArrayList<>();
        VoiceChannel currentChannel = bot.getJda().getGuildById(guildId).getAudioManager().getConnectedChannel();
        if (currentChannel != null) {
            List<Member> connectedUsers = currentChannel.getMembers();
            userList.addAll(connectedUsers.stream().filter(user -> !user.getUser().isBot() && !user.getVoiceState().isDeafened() &&
                    GuildSettings.get(currentChannel.getGuild()).canUseMusicCommands(user.getUser(), bot.security.getSimpleRankForGuild(user.getUser(), currentChannel.getGuild()))
            ).collect(Collectors.toList()));
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
        VoiceChannel currentChannel = bot.getJda().getGuildById(guildId).getAudioManager().getConnectedChannel();
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

    public JDA getJDA() {
        return bot.getJda();
    }

    public synchronized void stopAfterTrack(boolean stopAfter) {
        this.stopAfterTrack = stopAfter;
    }


    public class TrackScheduler extends AudioEventAdapter {
        private final AudioPlayer player;
        private final BlockingQueue<QueuedAudioTrack> queue;
        private volatile String lastRequester = "";

        public TrackScheduler(AudioPlayer player) {
            this.player = player;
            this.queue = new LinkedBlockingQueue<>();
        }

        public void queue(QueuedAudioTrack track) {
            if (queue.isEmpty()) {
                lastRequester = track.getUserId();
            }
            if (!player.startTrack(track.getTrack(), true)) {
                queue.offer(track);
            }
        }

        public synchronized String getLastRequester() {
            return lastRequester;
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            trackStarted();
        }

        public void skipTrack() {
            trackEnded();
            if (isInRepeatMode() && player.getPlayingTrack() != null) {
                player.startTrack(player.getPlayingTrack().makeClone(), false);
                return;
            }
            player.stopTrack();
            QueuedAudioTrack poll = queue.poll();
            if (poll != null) {
                lastRequester = poll.getUserId();
                player.startTrack(poll.getTrack(), false);
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
