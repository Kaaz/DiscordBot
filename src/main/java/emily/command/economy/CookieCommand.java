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

import emily.core.AbstractCommand;
import emily.db.controllers.CBanks;
import emily.db.controllers.CUser;
import emily.db.model.OBank;
import emily.db.model.OUser;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.util.TimeUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CookieCommand extends AbstractCommand {

    @Override
    public String getDescription() {
        return "Ask for a cookie";
    }

    @Override
    public String getCommand() {
        return "cookie";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "cookie             //gives you a cookie"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "candy", "cookies"
        };
    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        OUser user = CUser.findBy(author.getId());
        if (user.id == 0) {
            user.discord_id = author.getId();
            user.name = author.getName();
            CUser.insert(user);
        }
        OBank userAccount = CBanks.findBy(author.getId());
        if (userAccount.currentBalance > CBanks.CURRENCY_NO_HELP_AFTER) {
            return "not helping you anymore";
        }
        double now = (System.currentTimeMillis() / 1000D);
        double time = now - user.lastCurrencyRetrieval;
        int income = (int) Math.min(time * (CBanks.CURRENCY_PER_HOUR / 3600D), CBanks.CURRENCY_GIVEAWAY_MAX);
        int lastCurrencyRetrieval = user.lastCurrencyRetrieval;
        if (income == 0) {
            return String.format("no %s for you yet, try again in %s",
                    BotConfig.ECONOMY_CURRENCY_ICON, TimeUtil.getRelativeTime((long) (now + 1 + CBanks.SECONDS_PER_CURRENCY - (now - lastCurrencyRetrieval)), false, false))
                    + getFooter();
        }
        if (income == CBanks.CURRENCY_GIVEAWAY_MAX) {
            lastCurrencyRetrieval = (int) now;
        } else {
            lastCurrencyRetrieval += income * CBanks.SECONDS_PER_CURRENCY;
        }
        if (!CBanks.getBotAccount().transferTo(userAccount, income, "Fresh from the oven")) {
            Launcher.logToDiscord(new Exception("BANK_TRANSFER"), "from", "bot", "toAccount", userAccount.id);
        }
        user.lastCurrencyRetrieval = lastCurrencyRetrieval;
        CUser.update(user);
        return String.format("you get %s %s!", income, income == 1 ? BotConfig.ECONOMY_CURRENCY_NAME : BotConfig.ECONOMY_CURRENCY_NAMES) +
                getFooter();
    }

    private String getFooter() {
        return BotConfig.EOL +
                String.format("You can retrieve a %s every %s minutes, you don't have to retrieve them directly, I'll save up to %s %s for you.",
                        BotConfig.ECONOMY_CURRENCY_NAME, (int) (CBanks.SECONDS_PER_CURRENCY / 60), CBanks.CURRENCY_GIVEAWAY_MAX, BotConfig.ECONOMY_CURRENCY_NAMES);
    }
}
