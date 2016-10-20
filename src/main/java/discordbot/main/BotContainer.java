package discordbot.main;

import javax.security.auth.login.LoginException;

/**
 * Shared information between bots
 */
public class BotContainer {
	private final int numShards;
	private final DiscordBot[] shards;

	public BotContainer(int numGuilds) throws LoginException, InterruptedException {
		this.numShards = 1 + ((numGuilds + 1000) / 2500);
		shards = new DiscordBot[numShards];
		initShards();
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

	public DiscordBot getPMShard() {
		return shards[0];
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
