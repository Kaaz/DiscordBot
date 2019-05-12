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
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.main.BotConfig;
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
        if(args.length == 0) {
            step = 1;
            messageAuthor = author.getId();
            isRunning = true;
            channel.sendMessage("(Setting 1/9) What role would you like to be considered as the admin role? Type the name of the role without an @.").queue();
        } else if(args.length == 1 && args[0].equalsIgnoreCase("test")){
            listSettings(inputMessage);
        }else{
            return Templates.invalid_use.formatGuild(channel);
        }
        return "";
    }

    public static void nextMessage(String message, TextChannel channel){
        Guild guild = channel.getGuild();
        switch(step){
            case 1:
                List<Role> roles = channel.getGuild().getRolesByName(message, true);
                if(roles.size() == 0){
                    channel.sendMessage("No role with name `" + message + "` found in this server. Enter again.").queue();
                }else{
                    GuildSettings.get(guild).set(guild, "BOT_ADMIN_ROLE", message);
                    ++step;
                    channel.sendMessage("(Setting 2/9) Use internal error names (such as `invalid_use`) if commands are used incorrectly? Yes or no.").queue();
                }
                break;
            case 2:
                if(!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("no")){
                    channel.sendMessage("Invalid response. Yes or no.").queue();
                    return;
                }else if(message.equalsIgnoreCase("Yes")){
                    GuildSettings.get(guild).set(guild, "SHOW_TEMPLATES", "true");
                }else if(message.equalsIgnoreCase("No")){
                    GuildSettings.get(guild).set(guild, "SHOW_TEMPLATES", "false");
                }

                ++step;
                channel.sendMessage("(Setting 3/9) Would you like me to delete template messages after sending? Available options are " +
                        "`yes` (always delete), `no` (never delete), and `nonstandard` (delete messages outside of the default channel.").queue();
                break;
            case 3:
                if(message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("no") || message.equalsIgnoreCase("nonstandard")){
                    GuildSettings.get(guild).set(guild, "CLEANUP_MESSAGES", message);
                    ++step;
                    channel.sendMessage("(Setting 4/9) Enter a prefix for commands. If you want to keep it the same, type `same`.").queue();
                }else{
                    channel.sendMessage("Invalid response. Yes, no, or nonstandard.").queue();
                }
                break;
            case 4:
                if(message.equalsIgnoreCase("same")){
                    ++step;
                    channel.sendMessage("(Setting 5/9) Do you want help commands send directly to you (as opposed to posting in this channel)? Yes or no.").queue();
                }else if(message.length() > 4){
                    channel.sendMessage("Too long! Prefix can only be 1-4 characters.").queue();
                }else{
                    GuildSettings.get(guild).set(guild, "COMMAND_PREFIX", message);
                    ++step;
                    channel.sendMessage("(Setting 5/9) Do you want help commands send directly to you (as opposed to posting in this channel)? Yes or no.").queue();
                }
                break;
            case 5:
                if(!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("no")){
                    channel.sendMessage("Invalid response. Yes or no.").queue();
                    return;
                }else if(message.equalsIgnoreCase("Yes")){
                    GuildSettings.get(guild).set(guild, "HELP_IN_PM", "true");
                }else if(message.equalsIgnoreCase("No")){
                    GuildSettings.get(guild).set(guild, "HELP_IN_PM", "false");
                }

                ++step;
                channel.sendMessage("(Setting 6/9) Do you want me to say \"command does not exist\" when using invalid commands? Yes or no.").queue();
                break;
            case 6:
                if(!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("no")){
                    channel.sendMessage("Invalid response. Yes or no.").queue();
                    return;
                }else if(message.equalsIgnoreCase("yes")){
                    GuildSettings.get(guild).set(guild, "SHOW_UNKNOWN_COMMANDS", "true");
                }else if(message.equalsIgnoreCase("no")) {
                    GuildSettings.get(guild).set(guild, "SHOW_UNKNOWN_COMMANDS", "false");
                }

                ++step;
                channel.sendMessage("(Setting 7/9) How long do you want me to wait (in milliseconds) before deleting my template messages (if you said yes to setting 3)?").queue();
                break;
            case 7:
                try{
                    BotConfig.DELETE_MESSAGES_AFTER = Long.parseLong(message);
                    ++step;
                    channel.sendMessage("(Setting 8/9) Enter a maximum volume for the music player (1-100).").queue();
                }catch(NumberFormatException e){
                    channel.sendMessage("Invalid response. Must be a number (in milliseconds).").queue();
                }
                break;
            case 8:
                try{
                    int size = Integer.parseInt(message);
                    if(size < 0 || size > 100){
                        channel.sendMessage("Invalid response. Must be 1-100.").queue();
                    }else{
                        BotConfig.MUSIC_MAX_VOLUME = size;
                        ++step;
                        channel.sendMessage("(Setting 9/9) Enter a maximum size for music playlists (1-50).").queue();
                    }
                }catch(NumberFormatException e){
                    channel.sendMessage("Invalid response. Must be a number 1-100.").queue();
                }
                break;
            case 9:
                try{
                    int size = Integer.parseInt(message);
                    if(size < 0 || size > 50){
                        channel.sendMessage("Invalid response. Must be 1-50.").queue();
                    }else{
                        BotConfig.MUSIC_MAX_PLAYLIST_SIZE = size;
                    }
                }catch(NumberFormatException e){
                    channel.sendMessage("Invalid response. Must be a number 1-50.").queue();
                }
                channel.sendMessage("Setup finished.").queue();
                isRunning = false;
                break;
            default:
                channel.sendMessage("unknown error ya' DINGUS").queue();

        }
    }

    private void listSettings(Message message){
        String[] settings = GuildSettings.get(message.getGuild()).getSettings();
        TextChannel channel = message.getTextChannel();

        String roleID = settings[GSetting.BOT_ADMIN_ROLE.ordinal()];
        String send = "Admin Role Name: " + (roleID == null ? "not set" : channel.getGuild().getRoleById(roleID).getName());

        String errorName = settings[GSetting.SHOW_TEMPLATES.ordinal()];
        send += "\nUse internal error names? " + (errorName == null ? GSetting.SHOW_TEMPLATES.getDefaultValue() : errorName);

        String delete = settings[GSetting.CLEANUP_MESSAGES.ordinal()];
        send += "\nDelete messages after sending? " + (delete == null ? GSetting.CLEANUP_MESSAGES.getDefaultValue() : delete);

        String prefix = settings[GSetting.COMMAND_PREFIX.ordinal()];
        send += "\nCommand prefix: " + (prefix == null ? GSetting.COMMAND_PREFIX.getDefaultValue() : prefix);

        String help = settings[GSetting.HELP_IN_PM.ordinal()];
        send += "\nSend help commands in DM? " + (help == null ? GSetting.HELP_IN_PM.getDefaultValue() : help);

        String unknown = settings[GSetting.SHOW_UNKNOWN_COMMANDS.ordinal()];
        send += "\nSay \"command does not exist\" on invalid commands? " + (unknown == null ? GSetting.SHOW_UNKNOWN_COMMANDS.getDefaultValue() : help);

        send += "\nHow long before cleaning up messages? " + BotConfig.DELETE_MESSAGES_AFTER;
        send += "\nMusic player maximum volume: " + BotConfig.MUSIC_MAX_VOLUME;
        send += "\nPlaylist maximum size: " + BotConfig.MUSIC_MAX_PLAYLIST_SIZE;
        channel.sendMessage(send).queue();
    }
}