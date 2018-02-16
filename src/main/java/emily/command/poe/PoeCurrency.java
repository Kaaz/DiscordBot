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

package emily.command.poe;

import emily.core.AbstractCommand;
import emily.db.controllers.CPoEToken;
import emily.db.model.OPoEToken;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.libpoe.model.StashTab;
import org.libpoe.model.item.Item;
import org.libpoe.model.property.MinMaxProperty;
import org.libpoe.model.property.Property;
import org.libpoe.net.AuthInfo;
import org.libpoe.net.DataReader;
import org.libpoe.util.League;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * !poeitem
 * Analyzes an item from path of exile
 */
public class PoeCurrency extends AbstractCommand {
    public PoeCurrency() {
        super();
    }

    @Override
    public String getDescription() {
        return "Returns a list of currency on your account";
    }

    @Override
    public String getCommand() {
        return "poec";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "poec                   //returns list of currency for default league",
                "poec token <token>     //sets the session token",
                "poec league <league>   //currency for league",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("token")) {
                OPoEToken token = CPoEToken.findBy(author.getId());
                token.session_id = args[1];
                CPoEToken.insertOrUpdate(token);
                return "Updated your token!";
            } else if (args[0].equalsIgnoreCase("league")) {
                return "not implemented yet sorry boys!";
            }
            return Templates.invalid_use.format();
        }
        OPoEToken token = CPoEToken.findBy(author.getId());
        AuthInfo account = new AuthInfo(token.session_id);
        DataReader reader = new DataReader(account);
        if (!reader.authenticate()) {
            return "Your token is not valid :(";
        }
        bot.out.sendAsyncMessage(channel, "Fetching data this might take a minute!", null);
        HashMap<String, Integer> currency = new HashMap<>();
        int max = 1;
        for (int i = 0; i < max; i++) {
            StashTab stashTab = null;
            try {
                stashTab = reader.getStashTab(League.ESSENCE.getId(), i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (stashTab != null) {
                if (max == 1) {
                    max = stashTab.getNumTabs();
                }
                System.out.println((i + 1) + " out of " + stashTab.getNumTabs());
                for (Item item : stashTab.getItems()) {
                    if (item.getTypeLine().toLowerCase().contains("orb")) {
                        Property property = item.getProperty("Stack Size");
                        if (property instanceof MinMaxProperty) {
                            MinMaxProperty p = (MinMaxProperty) property;
                            if (!currency.containsKey(item.getTypeLine())) {
                                currency.put(item.getTypeLine(), 0);
                            }
                            currency.put(item.getTypeLine(), currency.get(item.getTypeLine()) + p.getMinValue());
                        }
                    }
                }
            }
        }
        String text = "Checking your currency in PoE!" + "\n";
        List<List<String>> tbl = new ArrayList<>();
        Misc.sortByValue(currency).forEach((k, v) -> {
            ArrayList<String> row = new ArrayList<>();
            row.add(k);
            row.add(String.valueOf(v));
            tbl.add(row);
        });
        text += Misc.makeAsciiTable(Arrays.asList("Currency", "#"), tbl, null);
        return text;
    }
}