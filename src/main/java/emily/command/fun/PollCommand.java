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

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.util.Misc;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class PollCommand extends AbstractCommand {

    public PollCommand() {
        super();
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Strawpoll: propose a question and choices for the chat to vote on";
    }

    @Override
    public String getCommand() {
        return "poll";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "poll          //status of active poll ",
                "poll create <question> ;<option1>;<option2>;<etc.>   (max 8)",
                "              //creates a poll for the duration",
                "poll 1-9      //vote on the options",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        if (!PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
            return "need permission to add reactions";
        }
        if (args.length == 0) {
            return "show overview";
        }
        if (args[0].equalsIgnoreCase("create")) {
            //if poll is NOT going on
            String argument = "";
            for (int i = 1; i < args.length; i++) {
                argument += " " + args[i];
            }
            String[] split = argument.split(";");
            if (split.length < 3) {
                return "Invalid usage! Need at least 2 options " + getUsage()[1];
            }
            if (split[0].trim().length() < 3) {
                return Template.get("command_poll_question_too_short");
            }
            String outtext = "A poll has been created by " + author.getName() + BotConfig.EOL + BotConfig.EOL;
            outtext += "**" + split[0].trim() + "**" + BotConfig.EOL + BotConfig.EOL;
            final int answers = Math.min(8, split.length);
            for (int i = 1; i < answers; i++) {
                outtext += Misc.numberToEmote(i) + " " + split[i].trim() + BotConfig.EOL + BotConfig.EOL;
            }
            bot.queue.add(channel.sendMessage(outtext), message -> {
                for (int i = 1; i < answers; i++) {
                    message.addReaction(Misc.numberToEmote(i)).complete();
                }
            });
        }
//		return Template.get("command_not_implemented");
        return "";
    }
}