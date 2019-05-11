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
import emily.handler.GuildSettings;
import emily.main.DiscordBot;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.*;

import java.util.List;

/**
 * !role
 * manages roles
 */
public class SetupCommand extends AbstractCommand {

    public static boolean isRunning;
    public static String type;
    private static int step;
    private static Guild guild;
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
                channel.sendMessage("What language? Available languages are `en` (english), `de` (german), and `nl` (dutch).").queue();
            }else if(args[0].equalsIgnoreCase("bot")){
                isRunning = true;
                type = "bot";
            }else{
                return Templates.invalid_use.formatGuild(channel);
            }
            return "";
        }
        return Templates.invalid_use.formatGuild(channel);
    }

    public static void nextMessage(String message, TextChannel channel){
        guild = channel.getGuild();
        switch(step){
            case 1:
                if(type.equalsIgnoreCase("guild")){
                    if (message.equalsIgnoreCase("en") || message.equalsIgnoreCase("de") || message.equalsIgnoreCase("nl")){
                        GuildSettings.get(guild).set(guild, "BOT_LANGUAGE", message);
                        ++step;
                        channel.sendMessage("What role would you like to be considered as the admin role? Type the name of the role without an @.").queue();
                    }else{
                        channel.sendMessage("Invalid language. Available languages are `en` (english), `de` (german), and `nl` (dutch). Enter again.").queue();
                        return;
                    }
                }else if(type.equalsIgnoreCase("bot")){
                    //TODO: DELETE_MESSAGES_AFTER
                    ++step;
                }
                break;
            case 2:
                if(type.equalsIgnoreCase("guild")){
                    List<Role> roles = channel.getGuild().getRolesByName(message, true);
                    if(roles.size() == 0){
                        channel.sendMessage("No role with name `" + message + "` found in this server. Enter again.").queue();
                    }else if(roles.size() > 0){
                        GuildSettings.get(guild).set(guild, "BOT_ADMIN_ROLE", message);
                        ++step;
                        channel.sendMessage("Use internal error names (such as `invalid_use`) if commands are used incorrectly? Yes or no.").queue();
                    }
                }else if(type.equalsIgnoreCase("bot")){
                    ++step;
                    //TODO: MUSIC_MAX_VOLUME
                }
                break;
            case 3:
                if(type.equalsIgnoreCase("guild")){
                    if(!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("no")){
                        channel.sendMessage("Invalid response. Yes or no.").queue();
                        return;
                    }else if(message.equalsIgnoreCase("Yes")){
                        GuildSettings.get(guild).set(guild, "SHOW_TEMPLATES", "true");
                    }else if(message.equalsIgnoreCase("No")){
                        GuildSettings.get(guild).set(guild, "SHOW_TEMPLATES", "false");
                    }
                    ++step;
                    channel.sendMessage("Would you like me to delete my messages after sending? Available options are " +
                            "`yes` (always delete), `no` (never delete), and `nonstandard` (delete messages outside of the default channel.").queue();
                }else if(type.equalsIgnoreCase("bot")){
                    ++step;
                    //TODO: COMMAND_LOGGING
                }
                break;
            case 4:
                if(type.equalsIgnoreCase("guild")){
                    if(message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("no") || message.equalsIgnoreCase("nonstandard")){
                        GuildSettings.get(guild).set(guild, "CLEANUP_MESSAGES", message);
                        ++step;
                        channel.sendMessage("Enter a prefix for commands. If you want to keep it the same, type `same`.").queue();
                    }else{
                        channel.sendMessage("Invalid response. Yes, no, or nonstandard.").queue();
                    }
                }else if(type.equalsIgnoreCase("bot")){
                    ++step;
                    //TODO: MAX_PLAYLIST_SIZE
                }
                break;
            case 5:
                if(type.equalsIgnoreCase("guild")){
                    if(message.equalsIgnoreCase("same")){
                        ++step;
                        channel.sendMessage("Do you want help commands send directly to you (as opposed to posting in this channel? Yes or no.").queue();
                    }else if(message.length() > 4){
                        channel.sendMessage("Too long! Prefix can only be 1-4 characters.").queue();
                    }else{
                        GuildSettings.get(guild).set(guild, "COMMAND_PREFIX", message);
                        ++step;
                        channel.sendMessage("Do you want help commands send directly to you (as opposed to posting in this channel? Yes or no.").queue();
                    }
                }else if(type.equalsIgnoreCase("bot")){
                    //TODO: ECONOMY_CURRENCY_NAME
                    isRunning = false; //only 5 settings for bot setup option
                    channel.sendMessage("Setup finished.").queue();
                }
                break;
            case 6:
                if(!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("no")){
                    channel.sendMessage("Invalid response. Yes or no.").queue();
                    return;
                }else if(message.equalsIgnoreCase("Yes")){
                    GuildSettings.get(guild).set(guild, "HELP_IN_PM", "true");
                }else if(message.equalsIgnoreCase("No")){
                    GuildSettings.get(guild).set(guild, "HELP_IN_PM", "false");
                }
                ++step;
                channel.sendMessage("Do you want the bot to say \"command does not exist\" when using invalid commands? Yes or no.").queue();
                break;
            case 7:
                if(message.equalsIgnoreCase("yes")){
                    GuildSettings.get(guild).set(guild, "SHOW_UNKNOWN_COMMANDS", "true");
                }else if(message.equals("no")) {
                    GuildSettings.get(guild).set(guild, "SHOW_UNKNOWN_COMMANDS", "false");
                }else {
                    channel.sendMessage("Invalid response. Yes or no.").queue();
                    return;
                }
                isRunning = false;
                channel.sendMessage("Setup finished.").queue();
                break;
            default:
                channel.sendMessage("invalid number ya' dingus").queue();

        }
    }
}