package discordbot.main;

import discordbot.core.ExitCode;

import javax.security.auth.login.LoginException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared information between bots
 */
public class BotContainer {
	private final int numShards;
	private final DiscordBot[] shards;
	private volatile AtomicInteger numGuilds;

	public BotContainer(int numGuilds) throws LoginException, InterruptedException {
		this.numShards = 1 + ((numGuilds + 1000) / 2500);
		shards = new DiscordBot[numShards];
		initShards();
		this.numGuilds = new AtomicInteger(numGuilds);
	}

	/**
	 * update the numguilds so that we can check if we need an extra shard
	 */
	public void guildJoined() {
		int suggestedShards = 1 + ((numGuilds.incrementAndGet() + 1000) / 2500);
		if (suggestedShards > numShards) {
			Launcher.stop(ExitCode.REBOOT);
		}
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
	 * checks if all shards are ready
	 *
	 * @return all shards ready
	 */
	public boolean allShardsReady() {
		for (DiscordBot shard : shards) {
			if (!shard.isReady()) {
				return false;
			}
		}
		return true;
	}
}
