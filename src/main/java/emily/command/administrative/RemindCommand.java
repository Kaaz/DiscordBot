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

import ch.lambdaj.function.aggregate.Min;
import emily.command.meta.AbstractCommand;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.TimeUtil;
import net.dv8tion.jda.core.entities.*;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.core.util.Integers;
import org.hamcrest.text.pattern.internal.ast.CharacterInRange;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * !remind
 * make the bot remind a channel after a given amount of time.
 */
public class RemindCommand extends AbstractCommand {
    public RemindCommand() {
        super();
    }

    @Override       //getDescription displayed when the user inputs: !help remind. describes the use for the remind command.
    public String getDescription() {
        return "Command that allows a user to remind a channel in the discord server by sending a message to that channel." +
                "The user who enters the command can also decide when the message will be sent(in minutes) to the channel.\n" +
                "Example being the command below which will send 'Team practice at 7:30' 10 minutes after the command was entered\n" +
                "Example: !remind general 10 Team practice at 7:30";
    }

    @Override
    public String getCommand() {
        return "remind";
    }

    @Override           //getUsage displayed when the user inputs: !help remind. Shows the format for the command.
    public String[] getUsage() {
        return new String[]{"remind <@channel> <@time> <message..>"};
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }



    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        String number = args[1];    //number entered by the user should be a int

        Guild currGuild = ((TextChannel) channel).getGuild();//Gets server that sent the command
        SimpleRank rank = bot.security.getSimpleRank(author, channel);

        if (!rank.isAtLeast(SimpleRank.USER)) {
            return Templates.no_permission.formatGuild(channel);
        }
        if (args.length > 2) {  //Makes sure the right amount of arguments where entered for the !remind command
            try {
                Integers.parseInt(number);  //Checks second input for a int if it fails the command fails
                long timer = Long.parseLong(number);   //timer is how long before the message is sent

                //retrieves channel user entered and fails if the channel entered is not part of the server
                TextChannel targetchannel =  currGuild.getTextChannelsByName(args[0],true).get(0);

                String message = "";
                for (int i = 2; i < args.length; i++) {
                    message += " " + args[i];
                }

                message = "Reminder from " + author.getName() + "! " + message;
                targetchannel.sendMessage(message).queueAfter(timer,TimeUnit.MINUTES); //sends message to channel after the entered time reaches zero

                return Templates.command.reminder_sent_success.formatGuild(channel);


            } catch(Exception e){   //happens if user did not enter a int for the second argument or channel entered cannot be found
                return Templates.command.reminder_failed_check_command_statement.formatGuild(channel);
            }
        }
        return Templates.invalid_use.formatGuild(channel);
    }
}