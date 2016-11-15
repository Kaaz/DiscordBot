package discordbot.main;

import discordbot.db.controllers.CBotPlayingOn;
import discordbot.db.model.OBotPlayingOn;
import discordbot.handler.*;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared information between bots
 */
public class BotContainer {
	private final int numShards;
	private final DiscordBot[] shards;
	private volatile AtomicInteger numGuilds;
	private volatile boolean needsMoreShards = false;
	private volatile boolean allShardsReady = false;

	public BotContainer(int numGuilds) throws LoginException, InterruptedException {
		this.numShards = 1 + ((numGuilds + 1000) / 2000);
		shards = new DiscordBot[numShards];
		initHandlers();
		initShards();
		this.numGuilds = new AtomicInteger(numGuilds);
	}

	/**
	 * update the numguilds so that we can check if we need an extra shard
	 */
	public void guildJoined() {
		int suggestedShards = 1 + ((numGuilds.incrementAndGet() + 500) / 2000);
		if (suggestedShards > numShards) {
			needsMoreShards = true;
		}
	}

	/**
	 * check if there are more shards required
	 *
	 * @return need more shards?
	 */
	public boolean needsMoreShards() {
		return needsMoreShards;
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
				MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
				player.connectTo(vc);
				player.playRandomSong();
			}
		}
		CBotPlayingOn.deleteAll();
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
}
