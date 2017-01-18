package discordbot.service;

import discordbot.command.ICommandCleanup;
import discordbot.core.AbstractCommand;
import discordbot.core.AbstractService;
import discordbot.handler.CommandHandler;
import discordbot.main.BotContainer;
import discordbot.main.DiscordBot;

import java.util.concurrent.TimeUnit;

/**
 * delete cached stuff, etc.
 */
public class BotCleanupService extends AbstractService {
	int runCount = 0;

	public BotCleanupService(BotContainer b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_cleanup_service";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(1);
	}

	@Override
	public boolean shouldIRun() {
		return bot.allShardsReady();
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		runCount++;
		for (DiscordBot shard : bot.getShards()) {
			if (shard == null || !shard.isReady()) {
				continue;
			}
			shard.commandReactionHandler.cleanCache();
			shard.chatBotHandler.cleanCache();
			shard.gameHandler.cleanCache();
		}
		if (runCount < 60) {
			return;
		}
		runCount = 0;
		for (AbstractCommand abstractCommand : CommandHandler.getCommandObjects()) {
			if (abstractCommand instanceof ICommandCleanup) {
				((ICommandCleanup) abstractCommand).cleanup();
			}
		}
		for (DiscordBot shard : bot.getShards()) {
			if (shard == null || !shard.isReady()) {
				continue;
			}
			shard.clearChannels();
		}

	}

	@Override
	public void afterRun() {
	}
}