package discordbot.service;

import discordbot.command.ICommandCleanup2;
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

	public BotCleanupService(BotContainer b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_cleanup_service";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(60);
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
		for (AbstractCommand abstractCommand : CommandHandler.getCommandObjects()) {
			if (abstractCommand instanceof ICommandCleanup2) {
				((ICommandCleanup2) abstractCommand).cleanup();
			}
		}
		for (DiscordBot shard : bot.getShards()) {
			shard.clearChannels();
		}

	}

	@Override
	public void afterRun() {
	}
}