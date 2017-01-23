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
			int shardId = shard.getShardId();
			long lastEventReceived = now - bot.getLastAction(shardId);
			if (lastEventReceived > RESTART_AFTER) {

				boolean restartSuccess = bot.tryRestartingShard(shardId);
				int limit = 9;
				while (!restartSuccess && --limit > 0) {
					try {
						Thread.sleep(15_000L);
						restartSuccess = bot.tryRestartingShard(shardId);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void afterRun() {
	}
}