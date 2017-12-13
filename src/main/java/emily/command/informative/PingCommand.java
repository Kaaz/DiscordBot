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
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !ping
 */
public class PingCommand extends AbstractCommand {
    public PingCommand() {
        super();
    }

    private static final String[] pingMessages = new String[]{
            ":ping_pong::white_small_square::black_small_square::black_small_square::ping_pong:",
            ":ping_pong::black_small_square::white_small_square::black_small_square::ping_pong:",
            ":ping_pong::black_small_square::black_small_square::white_small_square::ping_pong:",
            ":ping_pong::black_small_square::white_small_square::black_small_square::ping_pong:",
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {

        if (args.length > 0 && args[0].matches("fancy")) {
            bot.queue.add(channel.sendMessage("Checking ping..."), message -> {
                int pings = 5;
                int lastResult;
                int sum = 0, min = 999, max = 0;
                long start = System.currentTimeMillis();
                for (int j = 0; j < pings; j++) {
                    message.editMessage(pingMessages[j % pingMessages.length]).complete();
                    lastResult = (int) (System.currentTimeMillis() - start);
                    sum += lastResult;
                    min = Math.min(min, lastResult);
                    max = Math.max(max, lastResult);
                    try {
                        Thread.sleep(1_500L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    start = System.currentTimeMillis();
                }
                message.editMessage(String.format("Average ping is %dms (min: %d, max: %d)", (int)Math.ceil(sum / 5f), min, max)).complete();
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
