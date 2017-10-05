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
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.util.DisUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class DonateCommand extends AbstractCommand {
    public DonateCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "general info about how to contribute or donate to " + BotConfig.BOT_NAME;
    }

    @Override
    public String getCommand() {
        return "donate";
    }

    @Override
    public boolean isEnabled() {
        return BotConfig.CREATOR_ID.equals("97433066384928768");
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
    }

    @Override
    public String[] getAliases() {
        return new String[]{"contribute"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        String prefix = DisUtil.getCommandPrefix(channel);
        return "You're interested in contributing, that's great!\n \n" +
                "**Found a bug!**\n" +
                "You can report them on either *" + prefix + "discord* or *" + prefix + "github*\n \n" +
                "**Want to contribute or share your thoughts?**\n" +
                "Feel free to join *" + prefix + "discord* and let your voice be heard! Feedback and suggestions are always welcome!\n \n" +
                "**You know how to speak 0101?**\n" +
                "Check out *" + prefix + "github* and feel free to pick up one of the open issues\n \n" +
                "If you've ascended beyond 0101 and know multiple numbers, consider following the project on github to see whats happening\n \n" +
                "**You'd like to donate?**\n" +
                "You can do this though patreon <https://www.patreon.com/emilybot>\n \n";
    }
}