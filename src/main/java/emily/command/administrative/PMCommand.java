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

package emily.command.administrative;

import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.DisUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !pm
 * make the bot pm someone
 */
public class PMCommand extends AbstractCommand {
    public PMCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Send a message to user";
    }

    @Override
    public String getCommand() {
        return "pm";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"pm <@user> <message..>"};
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        if (!rank.isAtLeast(SimpleRank.USER)) {
            return Template.get("command_no_permission");
        }
        if (args.length > 1) {
            User targetUser = DisUtil.findUser((TextChannel) channel, args[0]);

            if (targetUser != null && !targetUser.getId().equals(channel.getJDA().getSelfUser().getId())) {
                String message = "";
                for (int i = 1; i < args.length; i++) {
                    message += " " + args[i];
                }
                bot.out.sendPrivateMessage(targetUser, "You got a message from " + author.getAsMention() + ": " + message);
                return Template.get("command_pm_success");
            } else {
                return Template.get("command_pm_cant_find_user");
            }
        }
        return Template.get("command_invalid_use");
    }
}