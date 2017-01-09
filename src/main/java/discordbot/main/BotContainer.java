package discordbot.main;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import discordbot.core.ExitCode;
import discordbot.db.controllers.CBotPlayingOn;
import discordbot.db.model.OBotPlayingOn;
import discordbot.handler.CommandHandler;
import discordbot.handler.GameHandler;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.SecurityHandler;
import discordbot.handler.Template;
import discordbot.role.RoleRankings;
import discordbot.threads.YoutubeThread;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
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
	private final AtomicLongArray lastActions;
	private final ScheduledExecutorService scheduler;


	public BotContainer(int numGuilds) throws LoginException, InterruptedException, RateLimitedException {
		scheduler = Executors.newScheduledThreadPool(3);
		this.numGuilds = new AtomicInteger(numGuilds);
		youtubeThread = new YoutubeThread();
		this.numShards = getRecommendedShards();
		shards = new DiscordBot[numShards];
		lastActions = new AtomicLongArray(numShards);
		initHandlers();
		initShards();
	}

	/**
	 * restarts a shard
	 *
	 * @param shardId the shard to restart
	 * @return true if it restarted
	 */
	public synchronized boolean tryRestartingShard(int shardId) {
		try {
			restartShard(shardId);
		} catch (InterruptedException | LoginException | RateLimitedException e) {
			BotContainer.LOGGER.error("rebootshard failed", e);
			Launcher.logToDiscord(e, "shard-restart", "failed", "shard-id", shardId);
			return false;
		}
		return true;
	}

	/**
	 * Schedule the a task somewhere in the future
	 *
	 * @param task     the task
	 * @param delay    the delay
	 * @param timeUnit unit type of delay
	 */
	public void schedule(Runnable task, Long delay, TimeUnit timeUnit) {
		scheduler.schedule(task, delay, timeUnit);
	}

	/**
	 * schedule a repeating task
	 *
	 * @param task        the taks
	 * @param startDelay  delay before starting the first iteration
	 * @param repeatDelay delay between consecutive executions
	 */
	public ScheduledFuture<?> scheduleRepeat(Runnable task, long startDelay, long repeatDelay) {
		return scheduler.scheduleWithFixedDelay(task, startDelay, repeatDelay, TimeUnit.MILLISECONDS);
	}

	/**
	 * restarts a shard
	 *
	 * @param shardId the shard to restart
	 * @throws InterruptedException
	 * @throws LoginException
	 * @throws RateLimitedException
	 */
	public synchronized void restartShard(int shardId) throws InterruptedException, LoginException, RateLimitedException {
		for (Guild guild : shards[shardId].client.getGuilds()) {
			MusicPlayerHandler.removeGuild(guild, true);
		}
		shards[shardId].client.shutdownNow(false);
		Thread.sleep(5_000L);
		shards[shardId] = new DiscordBot(shardId, shards.length, this);
		List<OBotPlayingOn> radios = CBotPlayingOn.getAll();
		for (OBotPlayingOn radio : radios) {
			if (calcShardId(Long.parseLong(radio.guildId)) != shardId) {
				continue;
			}
			Guild guild = shards[shardId].client.getGuildById(radio.guildId);
			if (guild != null) {
				VoiceChannel channel = guild.getVoiceChannelById(radio.channelId);
				if (channel != null) {
					boolean hasUsers = false;
					for (Member user : channel.getMembers()) {
						if (!user.getUser().isBot()) {
							hasUsers = true;
							break;
						}
					}
					if (hasUsers) {
						MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, shards[shardId]);
						player.connectTo(channel);
						if (!player.isPlaying()) {
							player.playRandomSong();
						}
					}
				}
			}
			CBotPlayingOn.deleteGuild(radio.guildId);
		}
		reportError(String.format("Quick, shard `%02d` is on %s, where are the %s'? Restarting the shard, off we go %s!",
				shardId, Emojibet.FIRE, Emojibet.FIRE_TRUCK, Emojibet.ROCKET));
	}

	public void setLastAction(int shard, long timestamp) {
		lastActions.set(shard, timestamp);
	}

	public long getLastAction(int shard) {
		return lastActions.get(shard);
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
	 * report an error to the configured error channel
	 *
	 * @param error   the Exception
	 * @param details extra details about the error
	 */

	public void reportError(Throwable error, Object... details) {
		String errorMessage = "I've encountered a **" + error.getClass().getName() + "**" + Config.EOL;
		if (error.getMessage() != null) {
			errorMessage += "Message: " + Config.EOL;
			errorMessage += error.getMessage() + Config.EOL + Config.EOL;
		}
		String stack = "";
		int maxTrace = 8;
		StackTraceElement[] stackTrace1 = error.getStackTrace();
		for (int i = 0; i < stackTrace1.length; i++) {
			StackTraceElement stackTrace = stackTrace1[i];
			stack += stackTrace.toString() + Config.EOL;
			if (i > maxTrace) {
				break;
			}
		}
		if (details.length > 0) {
			errorMessage += "Extra information: " + Config.EOL;
			for (int i = 1; i < details.length; i += 2) {
				if (details[i] != null) {
					errorMessage += details[i - 1] + " = " + details[i] + Config.EOL;
				} else if (details[i - 1] != null) {
					errorMessage += details[i - 1];
				}
			}
			errorMessage += Config.EOL + Config.EOL;
		}
		errorMessage += "Accompanied stacktrace: " + Config.EOL + Misc.makeTable(stack) + Config.EOL;
		reportError(errorMessage);
	}

	private void reportError(String message) {
		DiscordBot shard = getShardFor(Config.BOT_GUILD_ID);
		Guild guild = shard.client.getGuildById(Config.BOT_GUILD_ID);
		if (guild == null) {
			LOGGER.warn("Can't find BOT_GUILD_ID " + Config.BOT_GUILD_ID);
			return;
		}
		TextChannel channel = guild.getTextChannelById(Config.BOT_ERROR_CHANNEL_ID);
		if (channel == null) {
			LOGGER.warn("Can't find BOT_ERROR_CHANNEL_ID " + Config.BOT_ERROR_CHANNEL_ID);
			return;
		}
		channel.sendMessage(message).queue();
	}

	public void reportStatus(int shardId, JDA.Status oldStatus, JDA.Status status) {
		DiscordBot shard = getShardFor(Config.BOT_GUILD_ID);
		Guild guild = shard.client.getGuildById(Config.BOT_GUILD_ID);
		if (guild == null) {
			LOGGER.warn("Can't find BOT_GUILD_ID " + Config.BOT_GUILD_ID);
			return;
		}
		TextChannel channel = guild.getTextChannelById(Config.BOT_STATUS_CHANNEL_ID);
		if (channel == null) {
			LOGGER.warn("Can't find BOT_STATUS_CHANNEL_ID " + Config.BOT_STATUS_CHANNEL_ID);
			return;
		}
		if (!status.equals(JDA.Status.SHUTTING_DOWN)) {
			int length = 1 + (int) Math.floor(Math.log10(shards.length));
			channel.sendMessage(String.format(Emojibet.SHARD_ICON + " `%0" + length + "d/%0" + length + "d` | ~~%s~~ -> %s", shardId, shards.length, oldStatus.toString(), status.toString())).queue();
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
	 * {@link BotContainer#getShardFor(long)}
	 */
	public DiscordBot getShardFor(String discordGuildId) {
		if (numShards == 1) {
			return shards[0];
		}
		return getShardFor(Long.parseLong(discordGuildId));
	}

	/**
	 * Retrieves the right shard for the guildId
	 *
	 * @param discordGuildId the discord guild id
	 * @return the instance responsible for the guild
	 */
	public DiscordBot getShardFor(long discordGuildId) {
		if (numShards == 1) {
			return shards[0];
		}
		return shards[calcShardId(discordGuildId)];
	}

	/**
	 * calculate to which shard the guild goes to
	 *
	 * @param discordGuildId discord guild id
	 * @return shard number
	 */
	public int calcShardId(long discordGuildId) {
		return (int) ((discordGuildId >> 22) % numShards);
	}

	/**
	 * creates a new instance for each shard
	 *
	 * @throws LoginException       can't log in
	 * @throws InterruptedException ¯\_(ツ)_/¯
	 */
	private void initShards() throws LoginException, InterruptedException, RateLimitedException {
		for (int i = 0; i < shards.length; i++) {
			LOGGER.info("Starting shard #{} of {}", i, shards.length);
			shards[i] = new DiscordBot(i, shards.length, this);
			Thread.sleep(5_000L);
		}
		for (DiscordBot shard : shards) {
			setLastAction(shard.getShardId(), System.currentTimeMillis());
		}
	}

	/**
	 * After the bot is ready to go; reconnect to the voicechannels and start playing where it left off
	 */
	private void onAllShardsReady() {
		youtubeThread.start();
//		List<OBotPlayingOn> radios = CBotPlayingOn.getAll();
//		for (OBotPlayingOn radio : radios) {
//			DiscordBot bot = getShardFor(radio.guildId);
//			Guild guild = bot.client.getGuildById(radio.guildId);
//			VoiceChannel vc = null;
//			if (guild == null) {
//				continue;
//			}
//			for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
//				if (voiceChannel.getId().equals(radio.channelId)) {
//					vc = voiceChannel;
//					break;
//				}
//			}
//			if (vc != null) {
//				boolean hasUsers = false;
//				for (Member user : vc.getMembers()) {
//					if (!user.getUser().isBot()) {
//						hasUsers = true;
//						break;
//					}
//				}
//				if (hasUsers) {
//					MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
//					player.connectTo(vc);
//					if (!player.isPlaying()) {
//						player.playRandomSong();
//					}
//				}
//			}
//		}
		CBotPlayingOn.deleteAll();
	}

	private void initHandlers() {
		CommandHandler.initialize();
		GameHandler.initialize();
		SecurityHandler.initialize();
		Template.initialize();
		MusicPlayerHandler.init();
		RoleRankings.init();
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
