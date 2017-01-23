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

package discordbot.command.economy;

import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBanks;
import discordbot.db.model.OBank;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class BankCommand extends AbstractCommand {
	public BankCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "For all your banking needs";
	}

	@Override
	public String getCommand() {
		return "bank";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"jar                       //shows current balance",
				"jar history               //shows last transactions",
				"jar donate @user <amount> //donates <amount> to @user ",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"currency",
				"money",
				"jar",
		};
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		OBank bank = CBanks.findBy(author.getId());

		return String.format("Your current balance is `%s` %s ", bank.currentBalance, Config.ECONOMY_CURRENCY_ICON);
	}
}
