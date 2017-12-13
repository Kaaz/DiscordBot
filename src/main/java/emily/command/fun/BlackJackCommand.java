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

package emily.command.fun;

import emily.core.AbstractCommand;
import emily.games.Blackjack;
import emily.handler.Template;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.util.DisUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * !BlackJack
 * play a game of blackjack with the bot
 */
public class BlackJackCommand extends AbstractCommand {
    public final long DEALER_TURN_INTERVAL = 2000L;
    private Map<String, Blackjack> playerGames = new ConcurrentHashMap<>();

    public BlackJackCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "play a game of blackjack!";
    }

    @Override
    public String getCommand() {
        return "blackjack";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "blackjack        //check status",
                "blackjack hit    //hits",
                "blackjack stand  //stands",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "bj"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (args.length == 0) {
            if (playerGames.containsKey(author.getId()) && playerGames.get(author.getId()).isInProgress()) {
                return "You are still in a game. To finish type **blackjack stand**" + BotConfig.EOL +
                        playerGames.get(author.getId()).toString();
            }
            return "You are not playing a game, to start use **" + DisUtil.getCommandPrefix(channel) + "blackjack hit**";
        }
        if (args[0].equalsIgnoreCase("hit")) {
            if (!playerGames.containsKey(author.getId()) || !playerGames.get(author.getId()).isInProgress()) {
                playerGames.put(author.getId(), new Blackjack(author.getAsMention()));
            }
            if (playerGames.get(author.getId()).isInProgress() && !playerGames.get(author.getId()).playerIsStanding()) {
                playerGames.get(author.getId()).hit();
                return playerGames.get(author.getId()).toString();
            }
            return "";
        } else if (args[0].equalsIgnoreCase("stand")) {
            if (playerGames.containsKey(author.getId())) {
                final Future<?>[] f = {null};
                if (!playerGames.get(author.getId()).playerIsStanding()) {
                    bot.queue.add(channel.sendMessage(playerGames.get(author.getId()).toString()), message -> {
                        playerGames.get(author.getId()).stand();
                        f[0] = bot.scheduleRepeat(() -> {
                            boolean didHit = playerGames.get(author.getId()).dealerHit();
                            bot.queue.add(message.editMessage(playerGames.get(author.getId()).toString()));
                            if (!didHit) {
                                playerGames.remove(author.getId());
                                f[0].cancel(false);
                            }
                        }, DEALER_TURN_INTERVAL, DEALER_TURN_INTERVAL);
                    });
                }
                return "";
            }
            return "You are not playing a game, to start use **" + DisUtil.getCommandPrefix(channel) + "blackjack hit**";
        }

        return Template.get("command_invalid_use");
    }
}