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

package emily.command.bot_administration;

import com.google.common.base.Joiner;
import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !changename
 * changes the bots name
 */
public class ChangeName extends AbstractCommand {
    public ChangeName() {
        super();
    }

    @Override
    public String getDescription() {
        return "Changes my name";
    }

    @Override
    public String getCommand() {
        return "changename";
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        SimpleRank rank = bot.security.getSimpleRank(author);
        if (!rank.isAtLeast(SimpleRank.CREATOR)) {
            return Templates.no_permission.formatGuild(channel);
        }
        if (args.length > 0) {
            bot.setUserName(Joiner.on(" ").join(args));
            return "You can call me **" + Joiner.on(" ").join(args) + "** from now :smile:";
        }
        return ":face_palm: I expected you to know how to use it";
    }
}