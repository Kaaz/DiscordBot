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

package emily.command.creator;

import com.google.common.base.Joiner;
import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.File;

/**
 */
public class SendFileCommand extends AbstractCommand {
    public SendFileCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "executes commandline stuff";
    }

    @Override
    public String getCommand() {
        return "sendfile";
    }

    @Override
    public boolean isListed() {
        return true;
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
        if (!rank.isAtLeast(SimpleRank.SYSTEM_ADMIN)) {
            return Templates.no_permission.formatGuild(channel);
        }
        if (args.length == 0) {
            return Templates.invalid_use.formatGuild(channel);
        }
        File f = new File(Joiner.on("").join(args));
        if (f.exists()) {
            try {
                bot.queue.add(channel.sendFile(f));
            } catch (Exception e) {
                e.printStackTrace();
                return "can't for some reason; " + e.getMessage();
            }
            return "";
        }
        return "File doesn't exist";
    }
}