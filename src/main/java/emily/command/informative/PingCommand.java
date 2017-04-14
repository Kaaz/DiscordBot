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

package emily.command.informative;

import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.Future;

/**
 * !ping
 */
public class PingCommand extends AbstractCommand {
    public PingCommand() {
        super();
    }

    int i = 0;
    String[] pingMessages = new String[]{
            ":ping_pong::white_small_square::black_small_square::black_small_square::ping_pong:",
            ":ping_pong::black_small_square::white_small_square::black_small_square::ping_pong:",
            ":ping_pong::black_small_square::black_small_square::white_small_square::ping_pong:",
            ":ping_pong::black_small_square::white_small_square::black_small_square::ping_pong:",
            ":ping_pong::white_small_square::black_small_square::black_small_square::ping_pong:",
    };


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
        return new String[]{
                "ping                         //Check bot latency",
                "ping fancy                   //Check bot latency in a fancier way"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {

        if (args.length > 0 && args[0].matches("fancy")) {
            String[] pings = new String[6];
            pings[0] = "\nPing is: ...";
            bot.queue.add(channel.sendMessage("Checking ping..."), message -> {
                final Future<?>[] f = {null};
                f[0] = bot.scheduleRepeat(() -> {
                    try {
                        long finish = 0;
                        if (i < pingMessages.length) {
                            long start = System.currentTimeMillis();
                            message.editMessage(pingMessages[i] + pings[i]).complete();
                            finish = System.currentTimeMillis() - start;
                            i++;
                            pings[i] = ("\nPing is: " + Long.toString(finish));
                        } else {
                            int temp = 0;
                            for (int p = 1; p < pings.length; p++) {
                                String[] splitter = pings[p].split("(: )");
                                temp = temp + Integer.parseInt(splitter[1]);
                            }
                            int averagePing = (int) Math.ceil(temp / (pings.length - 1));
                            message.editMessage("Ping is: " + averagePing).complete();
                            i = 0;
                            f[0].cancel(true);
                        }

                    } catch (Exception e) {
                        System.out.println(e);
                        f[0].cancel(true);
                    }
                }, 100L, 1000L);
            });
        } else {
            long start = System.currentTimeMillis();
            bot.queue.add(channel.sendMessage(":outbox_tray: checking ping"),
                    message -> bot.queue.add(message.editMessage(":inbox_tray: ping is " + (System.currentTimeMillis() - start) + "ms")));
            return "";
        }
        return "";
    }
}

