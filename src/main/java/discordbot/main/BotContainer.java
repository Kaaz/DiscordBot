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
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Shared information between bots
 */
public class BotContainer {
	private final int numShards;
	private final DiscordBot[] shards;
	private volatile AtomicInteger numGuilds;
	private volatile boolean allShardsReady = false;
	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	private volatile boolean terminationRequested = false;
	private volatile ExitCode rebootReason = ExitCode.UNKNOWN;
	private final YoutubeThread youtubeThread;

	public BotContainer(int numGuilds) throws LoginException, InterruptedException {
		youtubeThread = new YoutubeThread();
		this.numShards = 1 + ((numGuilds + 1000) / 2000);
		shards = new DiscordBot[numShards];
		initHandlers();
		initShards();
		this.numGuilds = new AtomicInteger(numGuilds);
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
	private void initShards() throws LoginException, InterruptedException {
		for (int i = 0; i < shards.length; i++) {
			shards[i] = new DiscordBot(i, shards.length);
			shards[i].setContainer(this);
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
			for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
				if (voiceChannel.getId().equals(radio.channelId)) {
					vc = voiceChannel;
					break;
				}
			}
			if (vc != null) {
				boolean hasUsers = false;
				for (User user : vc.getUsers()) {
					if (!user.isBot()) {
						hasUsers = true;
						break;
					}
				}
				if (hasUsers) {
					MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
					player.connectTo(vc);
					player.playRandomSong();
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
			if (!shard.isReady()) {
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
}
