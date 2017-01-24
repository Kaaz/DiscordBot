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

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBankTransactions;
import discordbot.db.controllers.CBanks;
import discordbot.db.model.OBank;
import discordbot.db.model.OBankTransaction;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import discordbot.util.Misc;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

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
				"bank                       //shows current balance",
				"bank history               //shows last transactions",
				"bank send @user <amount>   //sends <amount> to @user ",
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
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		OBank bank = CBanks.findBy(author.getId());
		if (args.length == 0) {
			return String.format("Your current balance is `%s` %s ", bank.currentBalance, Config.ECONOMY_CURRENCY_ICON);
		}
		switch (args[0].toLowerCase()) {
			case "send":
			case "transfer":
				if (args.length < 3) {
					return Template.get("command_invalid_use");
				}
				int amount = Misc.parseInt(args[2], 0);
				if (amount < 1) {
					return Template.get("bank_transfer_minimum", 1, Config.ECONOMY_CURRENCY_NAME);
				}
				if (amount > bank.currentBalance) {
					return Template.get("bank_insufficient_funds", amount, amount == 1 ? Config.ECONOMY_CURRENCY_NAME : Config.ECONOMY_CURRENCY_NAMES);
				}
				User targetUser = DisUtil.findUser((TextChannel) channel, args[1]);
				if (targetUser == null) {
					return Template.get("cant_find_user", args[1]);
				}
				OBank targetBank = CBanks.findBy(targetUser.getId());
				if (bank.transferTo(targetBank, amount, "gift")) {
					return Template.get("bank_transfer_success", targetUser.getName(), amount, amount == 1 ? Config.ECONOMY_CURRENCY_NAME : Config.ECONOMY_CURRENCY_NAMES);
				}
				return Template.get("bank_transfer_failed");
			case "history":
				List<OBankTransaction> history = CBankTransactions.getHistoryFor(bank.id);
				String ret = "Your transaction history:\n \n";
				for (OBankTransaction transaction : history) {
					ret += String.format("%s `%s` `\u200B%4s` %s %s\n",
							TimeUtil.formatYMD(transaction.date),
							transaction.bankFrom == bank.id ? "SEND" : "RECV",
							transaction.amount,
							transaction.bankFrom == bank.id ? transaction.userTo : transaction.userFrom,
							transaction.description);
				}
				return ret;

			default:
				return Template.get("command_invalid_use");

		}
	}
}
