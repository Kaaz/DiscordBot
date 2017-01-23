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

package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !system
 * shows status of the bot's system
 */
public class SystemCommand extends AbstractCommand {
	public SystemCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Shows memory usage";
	}

	@Override
	public String getCommand() {
		return "system";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"sysinfo",
				"sys"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		final Runtime runtime = Runtime.getRuntime();
		StringBuilder sb = new StringBuilder();
		long memoryLimit = runtime.maxMemory();
		long memoryInUse = runtime.totalMemory() - runtime.freeMemory();

		sb.append("System information: ").append(Config.EOL);
		sb.append("Running version: ").append(Config.EOL).append(Launcher.getVersion()).append(Config.EOL);
		sb.append("Memory").append(Config.EOL);
		sb.append(getProgressbar(memoryInUse, memoryLimit));
		sb.append(" [ ").append(numberInMb(memoryInUse)).append(" / ").append(numberInMb(memoryLimit)).append(" ]").append(Config.EOL);
		return sb.toString();
	}

	private String getProgressbar(long current, long max) {
		String bar = "";
		final String BLOCK_INACTIVE = "▬";
		final String BLOCK_ACTIVE = ":large_blue_circle:";
		final int BLOCK_PARTS = 12;
		int activeBLock = (int) (((float) current / (float) max) * (float) BLOCK_PARTS);
		for (int i = 0; i < BLOCK_PARTS; i++) {
			if (i == activeBLock) {
				bar += BLOCK_ACTIVE;
			} else {
				bar += BLOCK_INACTIVE;
			}
		}
		return bar;
	}

	private String numberInMb(long number) {
		return "" + (number / (1048576L)) + " mb";
	}

}