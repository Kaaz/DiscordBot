package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.main.DiscordBot;

import java.util.concurrent.TimeUnit;

/**
 * Are shards still alive?
 */
public class ConnectionCheckerService extends AbstractService {

	public ConnectionCheckerService(BotContainer b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_connection_check_service";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.SECONDS.toMillis(30);
	}

	@Override
	public boolean shouldIRun() {
		return Config.BOT_RESTART_INACTIVE_SHARDS;
	}

	@Override
	public void beforeRun() {
	}

	private static final long RESTART_AFTER = TimeUnit.MINUTES.toMillis(1);

	@Override
	public void run() {
		DiscordBot[] shards = bot.getShards();
		final long now = System.currentTimeMillis();
		for (DiscordBot shard : shards) {
			if (shard == null || !shard.isReady()) {
				continue;
			}
			long lastEventReceived = now - bot.getLastAction(shard.getShardId());
			if (lastEventReceived > RESTART_AFTER) {
				bot.restartShard(shard.getShardId());
			}
		}
	}

	@Override
	public void afterRun() {
	}
}