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

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.Emojibet;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !emojify
 */
public class EmojifyCommand extends AbstractCommand {
    public static final int MAX_SIZE = 200;

    public EmojifyCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "turns everything what you say into emotes emotes";
    }

    @Override
    public String getCommand() {
        return "emojify";
    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "emojify <anything>",
                "example emojify hello world"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "emotify"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (args.length > 0) {
            String combined = Joiner.on(" ").join(args).toLowerCase();
            int strlen = combined.length();
            if (combined.length() > MAX_SIZE) {
                return Templates.command.emojify_max_exceeded.formatGuild(channel, MAX_SIZE);
            }
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < strlen; i++) {
                output.append(Emojibet.getEmojiFor(String.valueOf(combined.charAt(i))));
                output.append("\u200B");
            }
            return output.toString();
        } else {
            return Templates.command.SAY_WHATEXACTLY.formatGuild(channel);
        }
    }
}