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

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;

/**
 * !avatar
 * manage avatar
 */
public class ChangeAvatar extends AbstractCommand {
    public ChangeAvatar() {
        super();
    }

    @Override
    public String getDescription() {
        return "Changes my avatar";
    }

    @Override
    public String getCommand() {
        return "updateavatar";
    }

    @Override
    public boolean isListed() {
        return false;
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        SimpleRank rank = bot.security.getSimpleRank(author);

        if (!rank.isAtLeast(SimpleRank.CREATOR)) {
            return Template.get(channel, "command_no_permission");
        }
        if (args.length <= 1) {
            try {
                Icon icon = Icon.from(Unirest.get(args[0]).asBinary().getBody());
                bot.queue.add(channel.getJDA().getSelfUser().getManager().setAvatar(icon));
            } catch (IOException | UnirestException e) {
                return "Error: " + e.getMessage();
            }
            return ":+1:";
        }
        return ":face_palm: I expected you to know how to use it";
    }
}