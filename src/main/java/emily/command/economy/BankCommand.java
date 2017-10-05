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

package emily.command.economy;

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.db.controllers.CBankTransactions;
import emily.db.controllers.CBanks;
import emily.db.model.OBank;
import emily.db.model.OBankTransaction;
import emily.handler.Template;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
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
                "bank                                //shows current balance",
                "bank history                        //shows last transactions",
                "bank send @user <amount>            //sends <amount> to @user ",
                "bank send @user <amount> <message>  //sends <amount> to @user with a message",
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
            return String.format("Your current balance is `%s` %s ", bank.currentBalance, BotConfig.ECONOMY_CURRENCY_ICON);
        }
        switch (args[0].toLowerCase()) {
            case "send":
            case "transfer":
                if (args.length < 3) {
                    return Template.get("command_invalid_use");
                }
                int amount = Misc.parseInt(args[2], 0);
                if (amount < 1) {
                    return Template.get("bank_transfer_minimum", 1, BotConfig.ECONOMY_CURRENCY_NAME);
                }
                if (amount > bank.currentBalance) {
                    return Template.get("bank_insufficient_funds", amount, amount == 1 ? BotConfig.ECONOMY_CURRENCY_NAME : BotConfig.ECONOMY_CURRENCY_NAMES);
                }
                User targetUser = DisUtil.findUser((TextChannel) channel, args[1]);
                if (targetUser == null) {
                    return Template.get("cant_find_user", args[1]);
                }
                OBank targetBank = CBanks.findBy(targetUser.getId());
                String description = "Gift!";
                if (args.length > 3) {
                    description = Misc.joinStrings(args, 3);
                }
                if (bank.transferTo(targetBank, amount, description)) {
                    return Template.get("bank_transfer_success", targetUser.getName(), amount, amount == 1 ? BotConfig.ECONOMY_CURRENCY_NAME : BotConfig.ECONOMY_CURRENCY_NAMES);
                }
                return Template.get("bank_transfer_failed");
            case "history":
                List<OBankTransaction> history = CBankTransactions.getHistoryFor(bank.id);
                String ret = "Your transaction history:\n \n";
                for (OBankTransaction transaction : history) {
                    ret += String.format("%s`\u200B%+4d`%s`\u200B%24s`%s%s *%s*\n",
                            transaction.bankFrom == bank.id ? Emojibet.TRIANGLE_RED_DOWN : ":arrow_up_small:",
//                            transaction.bankFrom == bank.id ? Emojibet.TRIANGLE_RED_DOWN : Emojibet.INBOX_TRAY,
                            transaction.bankFrom == bank.id ? -transaction.amount : transaction.amount,
                            BotConfig.ECONOMY_CURRENCY_ICON,
                            transaction.bankFrom == bank.id ? transaction.userTo : transaction.userFrom,
                            transaction.bankFrom != bank.id ? ":arrow_left:":":arrow_right:",
                            transaction.bankFrom != bank.id ? Emojibet.USER : Emojibet.SPEECH_BALLOON,
                            transaction.description.substring(0, Math.min(25, transaction.description.length())));
                }
                return ret;

            default:
                return Template.get("command_invalid_use");

        }
    }
}
