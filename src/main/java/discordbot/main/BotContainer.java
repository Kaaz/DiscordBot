package discordbot.main;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import discordbot.core.ExitCode;
import discordbot.db.controllers.CBotPlayingOn;
import discordbot.db.model.OBotPlayingOn;
import discordbot.handler.*;
import discordbot.threads.YoutubeThread;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Shared information between bots
 */
public class BotContainer {
	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	private final int numShards;
	private final DiscordBot[] shards;
	private final YoutubeThread youtubeThread;
	private final AtomicBoolean statusLocked = new AtomicBoolean(false);
	private final AtomicInteger numGuilds;
	private volatile boolean allShardsReady = false;
	private volatile boolean terminationRequested = false;
	private volatile ExitCode rebootReason = ExitCode.UNKNOWN;

	public BotContainer(int numGuilds) throws LoginException, InterruptedException, RateLimitedException {
		this.numGuilds = new AtomicInteger(numGuilds);
		youtubeThread = new YoutubeThread();
		this.numShards = 5;//getRecommendedShards();
		shards = new DiscordBot[numShards];
		initHandlers();
		initShards();
	}

	/**
	 * Request that the bot exits
	 *
	 * @param reason the reason
	 */
	public synchronized void requestExit(ExitCode reason) {
		if (!terminationRequested) {
			terminationRequested = true;
			rebootReason = reason;
			youtubeThread.shutown();
		}
	}

	/**
	 * update the numguilds so that we can check if we need an extra shard
	 */
	public void guildJoined() {
		int suggestedShards = 1 + ((numGuilds.incrementAndGet() + 500) / 2000);
		if (suggestedShards > numShards) {
			terminationRequested = true;
			rebootReason = ExitCode.NEED_MORE_SHARDS;
		}
	}

	/**
	 * Retrieves the shard recommendation from discord
	 *
	 * @return recommended shard count
	 */
	public int getRecommendedShards() {
		try {
			HttpResponse<JsonNode> request = Unirest.get("https://discordapp.com/api/gateway/bot")
					.header("Authorization", "Bot " + Config.BOT_TOKEN)
					.header("Content-Type", "application/json")
					.asJson();
			return Integer.parseInt(request.getBody().getObject().get("shards").toString());
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return 1;
	}

	/**
	 * {@link BotContainer#guildJoined()}
	 */
	public void guildLeft() {
		numGuilds.decrementAndGet();
	}

	public DiscordBot[] getShards() {
		return shards;
	}

	/**
	 * {@link BotContainer#getBotFor(long)}
	 */
	public DiscordBot getBotFor(String discordGuildId) {
		if (numShards == 1) {
			return shards[0];
		}
		return getBotFor(Long.parseLong(discordGuildId));
	}

	/**
	 * Retrieves the right shard for the guildId
	 *
	 * @param discordGuildId the discord guild id
	 * @return the instance responsible for the guild
	 */
	public DiscordBot getBotFor(long discordGuildId) {
		if (numShards == 1) {
			return shards[0];
		}
		return shards[(int) ((discordGuildId >> 22) % numShards)];
	}

	/**
	 * creates a new instance for each shard
	 *
	 * @throws LoginException       can't log in
	 * @throws InterruptedException ¯\_(ツ)_/¯
	 */
	private void initShards() throws LoginException, InterruptedException, RateLimitedException {
		for (int i = 0; i < shards.length; i++) {
			shards[i] = new DiscordBot(i, shards.length, this);
			Thread.sleep(5_000L);
		}
	}

	/**
	 * After the bot is ready to go; reconnect to the voicechannels and start playing where it left off
	 */
	private void onAllShardsReady() {
		List<OBotPlayingOn> radios = CBotPlayingOn.getAll();
		for (OBotPlayingOn radio : radios) {
			DiscordBot bot = getBotFor(radio.guildId);
			Guild guild = bot.client.getGuildById(radio.guildId);
			VoiceChannel vc = null;
			if (guild == null) {
				continue;
			}
			for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
				if (voiceChannel.getId().equals(radio.channelId)) {
					vc = voiceChannel;
					break;
				}
			}
			if (vc != null) {
				boolean hasUsers = false;
				for (Member user : vc.getMembers()) {
					if (!user.getUser().isBot()) {
						hasUsers = true;
						break;
					}
				}
				if (hasUsers) {
					MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
					player.connectTo(vc);
					if (!player.isPlaying()) {
						player.playRandomSong();
					}
				}
			}
		}
		CBotPlayingOn.deleteAll();
		youtubeThread.start();
	}

	private void initHandlers() {
		CommandHandler.initialize();
		GameHandler.initialize();
		SecurityHandler.initialize();
		Template.initialize();
		MusicPlayerHandler.init();
	}

	/**
	 * checks if all shards are ready
	 *
	 * @return all shards ready
	 */
	public boolean allShardsReady() {
		if (allShardsReady) {
			return allShardsReady;
		}
		for (DiscordBot shard : shards) {
			if (shard == null || !shard.isReady()) {
				return false;
			}
		}
		allShardsReady = true;
		onAllShardsReady();
		return true;
	}

	public boolean isTerminationRequested() {
		return terminationRequested;
	}

	public ExitCode getRebootReason() {
		return rebootReason;
	}

	/**
	 * Queue up a track to fetch from youtube
	 *
	 * @param youtubeCode the video code
	 * @param message     message object
	 * @param callback    the callback
	 */
	public void downloadRequest(String youtubeCode, String youtubeTitle, Message message, Consumer<Message> callback) {
		youtubeThread.addToQueue(youtubeCode, youtubeTitle, message, callback);
	}

	/**
	 * how many tracks are in the queue to be processed?
	 *
	 * @return amount
	 */
	public int downloadsProcessing() {
		return youtubeThread.getQueueSize();
	}

	public synchronized boolean isInProgress(String videoCode) {
		return youtubeThread.isInProgress(videoCode);
	}

	/**
	 * Check if the bot's status is locked
	 * If its locked, the bot will not change its status
	 *
	 * @return locked?
	 */
	public boolean isStatusLocked() {
		return statusLocked.get();
	}

	/**
	 * Lock/unlock the bot's status
	 *
	 * @param locked lock?
	 */
	public void setStatusLocked(boolean locked) {
		statusLocked.set(locked);
	}
}
