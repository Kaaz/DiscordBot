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

package emily.service;

import emily.core.AbstractService;
import emily.main.BotContainer;
import emily.main.Config;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

/**
 * pseudo randomly sets the now playing tag of the bot
 */
public class BotStatusService extends AbstractService {
    private final static String[] statusList = {
            "with %s human pets",
            "Teaching %s Minions",
            "Bot simulator 2%03d",
            "Pokemon Go, got %s so far",
            "tipping %s cows",
            "Sorting commands, what comes after %s",
            "Planning for wold domination #%s",
            "Reading wikipedia.org/wiki/%s",
            "Talking to %s Martians",
            "Homework | %s assignments",
            "Hearthstone | rank %s",
            "Path of exile | level %s",
            "Blackjack with %s victims",
            "Half Life %s",
            "russian roulette | %s left",
            "hide and seek with %s users",
            "peekaboo",
            "\";DROP TABLE WHERE id = %s",
            "rating your waifu a %s",
            "Talking to %s idiots",
            "Looking for %s new jokes",
            "Organizing music",
            "Trying to remember preferences",
            "Analyzing %s fellow humans",
            "Yesterday you said tomorrow",
            "Let dreams be dreams",
            "finding Rare pepe #%s",
            "Megaman %s",
            "Having my %s minutes of fame",
            "Predicting %s minutes",
            "Achieving nirvana",
            "bending spoons, attempt #%s",
            "Making my top %s most wanted list",
            "Running %s miles",
            "Dancing the macarena",
            "Jousting #%s",
            "Fishing with %s poles",
    };
    private final Random rng;

    public BotStatusService(BotContainer b) {
        super(b);
        rng = new Random();
    }

    @Override
    public String getIdentifier() {
        return "bot_nickname";
    }

    @Override
    public long getDelayBetweenRuns() {
        return 300_000;
    }

    @Override
    public boolean shouldIRun() {
        return !bot.isStatusLocked();
    }

    @Override
    public void beforeRun() {
    }

    @Override
    public void run() {
        int roll = rng.nextInt(100);
        TextChannel inviteChannel = bot.getShardFor(Config.BOT_GUILD_ID).getJda().getTextChannelById(Config.BOT_CHANNEL_ID);
        if (inviteChannel != null && roll < 10) {
            String fallback = "Feedback @ https://discord.gg/eaywDDt | #%s";
            bot.getShardFor(Config.BOT_GUILD_ID).queue.add(inviteChannel.getInvites(),
                    invites -> {
                        if (invites != null && !invites.isEmpty()) {
                            setGameOnShards(bot, "Feedback @ https://discord.gg/" + invites.get(0).getCode() + " | %s");
                        } else {
                            setGameOnShards(bot, fallback);
                        }
                    });
        } else if (roll < 50) {
            String username = bot.getShards()[0].getJda().getSelfUser().getName();
            setGameOnShards(bot, "@" + username + " help | @" + username + " invite | #%s");
        } else {
            setGameOnShards(bot, statusList[new Random().nextInt(statusList.length)]);
        }
    }

    private void setGameOnShards(BotContainer container, String status) {
        for (DiscordBot shard : container.getShards()) {
            shard.getJda().getPresence().setGame(Game.of(String.format(status, shard.getShardId())));
        }
    }

    @Override
    public void afterRun() {
    }
}