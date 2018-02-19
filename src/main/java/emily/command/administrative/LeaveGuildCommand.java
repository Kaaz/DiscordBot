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
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * leaves the guild
 */
public class LeaveGuildCommand extends AbstractCommand {
    public LeaveGuildCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "leaves guild :(";
    }

    @Override
    public String getCommand() {
        return "leaveguild";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "leaveguild     //leaves the guild"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        boolean shouldLeave = false;
        Guild guild = ((TextChannel) channel).getGuild();
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Templates.no_permission.formatGuild(channel);
        }
        if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 1 && args[0].matches("^\\d{10,}$")) {
            guild = channel.getJDA().getGuildById(args[0]);
            if (guild == null) {
                return Templates.config.cant_find_guild.formatGuild(channel);
            }
            if (args.length == 1) {
                return "are you sure? :sob: type **`" + DisUtil.getCommandPrefix(channel) + "leaveguild " + args[0] + " confirm`** to leave _" + guild.getName() + "_";
            }
            if (args[1].equals("confirm")) {
                shouldLeave = true;
            }
        }
        if (args.length == 0) {
            return "are you sure? :sob: type **" + DisUtil.getCommandPrefix(channel) + "leaveguild confirm** to leave";
        }
        if (args[0].equals("confirm")) {
            shouldLeave = true;
        }
        if (shouldLeave) {
            Guild finalGuild = guild;
            bot.out.sendAsyncMessage(bot.getDefaultChannel(guild), "This is goodbye :wave:", message -> {
                bot.queue.add(finalGuild.leave());
            });
            return "";
        }
        return ":face_palm: I expected you to know how to use it";
    }
}