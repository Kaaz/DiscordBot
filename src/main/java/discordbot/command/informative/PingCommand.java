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

package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !ping
 */
public class PingCommand extends AbstractCommand {
	public PingCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "checks the latency of the bot";
	}

	@Override
	public String getCommand() {
		return "ping";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		long start = System.currentTimeMillis();
		channel.sendMessage(":outbox_tray: checking ping").queue(
				message -> message.editMessage(":inbox_tray: ping is " + (System.currentTimeMillis() - start) + "ms").queue()
		);
		return "";
	}
}