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

package discordbot.command.creator;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Date;

/**
 */
public class DebugCommand extends AbstractCommand {
    public DebugCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "some debugging tools";
    }

    @Override
    public String getCommand() {
        return "debug";
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "activity //shows last shard activity"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        if (!bot.security.getSimpleRank(author).isAtLeast(SimpleRank.CREATOR)) {
            return Template.get(channel, "command_no_permission");
        }
        if (args.length == 0) {
            return Emojibet.EYES;
        }
        switch (args[0].toLowerCase()) {
            case "activity":
                return lastShardActivity(bot.getContainer());
        }
        boolean value = false;
        boolean updating = args.length > 1;
        if (updating) {
            value = Misc.isFuzzyTrue(args[1]);
        }
        switch (args[0].toLowerCase()) {
            case "yt":
            case "youtube":
                if (updating) {
                    Config.YOUTUBEDL_DEBUG_PROCESS = value;
                }
                value = Config.YOUTUBEDL_DEBUG_PROCESS;
                break;
            default:
                return Emojibet.SHRUG;
        }
        if (updating) {
            return Emojibet.OKE_SIGN + " " + args[0] + " is set to " + value;
        }
        return Emojibet.UNLOCKED + " " + args[0] + " = `" + value + "`";
    }

    private String lastShardActivity(BotContainer container) {
        long now = System.currentTimeMillis();
        String msg = "Last event per shard: " + new Date(now).toString() + "\n\n";
        String comment = "";
        for (DiscordBot shard : container.getShards()) {
            if (shard == null || !shard.isReady()) {
                msg += "#shard is being reset and is reloading\n";
                continue;
            }
            long lastEventReceived = now - container.getLastAction(shard.getShardId());
            msg += String.format("#%02d: %s sec ago\n", shard.getShardId(), lastEventReceived / 1000L);
        }
        return msg + comment;
    }
}