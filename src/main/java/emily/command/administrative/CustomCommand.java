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

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.db.controllers.CGuild;
import emily.handler.CommandHandler;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Misc;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;

/**
 * Created on 11-8-2016
 */
public class CustomCommand extends AbstractCommand {
    private String[] valid_actions = {"add", "delete"};

    public CustomCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Add and remove custom commands." + "\n" +
                "There are a few keywords you can use in commands. These tags will be replaced by its value " + "\n" + "\n" +
                "Key                Replacement\n" +
                "---                ---\n" +
                "%user%             Username \n" +
                "%args%             everything the user said besides the command \n" +
                "%arg1%             the first argument of the user \n" +
                "%arg9%             the 9th argument etc. a new argument starts after a space \n" +
                "%user-mention%     Mentions user \n" +
                "%user-id%          ID of user\n" +
                "%nick%             Nickname\n" +
                "%discrim%          discrim\n" +
                "%guild%            Guild name\n" +
                "%guild-id%         guild id\n" +
                "%guild-users%      amount of users in the guild\n" +
                "%channel%          channel name\n" +
                "%channel-id%       channel id\n" +
                "%channel-mention%  Mentions channel\n" +
                "%rand-user%        random user in guild\n" +
                "%rand-user-online% random ONLINE user in guild";
    }

    @Override
    public String getCommand() {
        return "command";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "command add <command> <action>  //adds a command",
                "command delete <command>        //deletes a command",
                "command                         //shows a list of existing custom commands"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "cmd", "customcommand"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Templates.no_permission.format();
        }
        int guildId = CGuild.getCachedId(((TextChannel) channel).getGuild().getId());
        String prefix = DisUtil.getCommandPrefix(channel);
        if (args.length >= 2 && Arrays.asList(valid_actions).contains(args[0])) {
            if (args[0].equals("add") && args.length > 2) {
                StringBuilder output = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    output.append(args[i]).append(" ");
                }
                if (args[0].startsWith(prefix)) {
                    args[0] = args[0].substring(prefix.length());
                }
                CommandHandler.addCustomCommand(guildId, args[1], EmojiUtils.shortCodify(output.toString().trim()));
                return "Added " + prefix + args[1];
            } else if (args[0].equals("delete")) {
                CommandHandler.removeCustomCommand(guildId, args[1]);
                return "Removed " + prefix + args[1];
            }
        } else if (args.length == 0 || (args.length > 0 && args[0].equalsIgnoreCase("list"))) {
            return "All custom commands: " + "\n" + Misc.makeTable(CommandHandler.getCustomCommands(guildId));
        } else {
            return "```" + "\n" +
                    getDescription() + "\n" + "```";
        }
        return Templates.no_permission.format();
    }
}
