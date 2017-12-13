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

import emily.core.AbstractCommand;
import emily.db.controllers.CUser;
import emily.db.model.OUser;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * ban a user from a guild
 */
public class GlobalBanCommand extends AbstractCommand {
    public GlobalBanCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Ban those nasty humans";
    }

    @Override
    public String getCommand() {
        return "globalban";
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
        if (rank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 1) {
            boolean unban = args.length > 1 && Misc.isFuzzyFalse(args[1]);
            OUser user = CUser.findBy(args[0]);
            user.banned = unban ? 0 : 1;
            if (user.id == 0) {
                return "User `" + args[0] + "` not found";
            }
            CUser.update(user);
            if (unban) {
                bot.security.removeUserBan(Long.parseLong(user.discord_id));
                return "`" + user.name + "` (`" + user.discord_id + "`) has been globally unbanned";
            } else {
                bot.security.addUserBan(Long.parseLong(user.discord_id));
                return "`" + user.name + "` (`" + user.discord_id + "`) has been globally banned";
            }
        }
        return Template.get("command_no_permission");
    }
}