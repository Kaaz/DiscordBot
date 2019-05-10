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

import emily.command.meta.AbstractCommand;
import emily.command.meta.CommandVisibility;
import emily.main.DiscordBot;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !role
 * manages roles
 */
public class SetupCommand extends AbstractCommand {

    public static boolean isRunning;
    public static String type;
    private static int step;
    public static String messageAuthor;


    public SetupCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Guided setup of basic configuration settings.\n";
    }

    @Override
    public String getCommand() {
        return "setup";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "setup guild              //guided setup of guild-specific settings.",
                "setup bot                //guided setup of core bot settings."
        };
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if(args.length == 1){
            step = 1;
            messageAuthor = author.getId();
            if(args[0].equalsIgnoreCase("guild")){
                isRunning = true;
                type = "guild";
            }else if(args[0].equalsIgnoreCase("bot")){
                isRunning = true;
                type = "bot";
            }else{
                return Templates.invalid_use.formatGuild(channel);
            }
            return "Setup started";
        }
        return Templates.invalid_use.formatGuild(channel);
    }

    public static void nextMessage(String message, TextChannel channel){
        switch(step){
            case 1:
                if(type.equalsIgnoreCase("guild")){
                    ++step;
                    //TODO: BOT_LANGUAGE
                }else if(type.equalsIgnoreCase("bot")){
                    //TODO: DELETE_MESSAGES_AFTER
                    ++step;
                }
            case 2:
                if(type.equalsIgnoreCase("guild")){
                    ++step;
                    //TODO: BOT_ADMIN_ROLE
                }else if(type.equalsIgnoreCase("bot")){
                    ++step;
                    //TODO: MUSIC_MAX_VOLUME
                }
            case 3:
                if(type.equalsIgnoreCase("guild")){
                    ++step;
                    //TODO: SHOW_TEMPLATES
                }else if(type.equalsIgnoreCase("bot")){
                    ++step;
                    //TODO: COMMAND_LOGGING
                }
            case 4:
                if(type.equalsIgnoreCase("guild")){
                    ++step;
                    //TODO: CLEANUP_MESSAGES
                }else if(type.equalsIgnoreCase("bot")){
                    ++step;
                    //TODO: MAX_PLAYLIST_SIZE
                }
            case 5:
                if(type.equalsIgnoreCase("guild")){
                    ++step;
                    //TODO: COMMAND_PREFIX
                }else if(type.equalsIgnoreCase("bot")){
                    //TODO: ECONOMY_CURRENCY_NAME
                    isRunning = false; //only 5 settings for bot setup option
                    channel.sendMessage("Setup finished.").queue();
                }
            case 6:
                //no need to check for guild/bot here since only 5 bot options, if here it must be guild
                //TODO: HELP_IN_PM
            case 7:
                //TODO: SHOW_UNKNOWN_COMMANDS
                isRunning = false;
                channel.sendMessage("Setup finished.").queue();

        }
    }
}