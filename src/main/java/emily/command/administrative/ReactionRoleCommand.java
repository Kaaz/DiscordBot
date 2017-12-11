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
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.Misc;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

/**
 * give and take away roles with reactions rather than typing
 */
public class ReactionRoleCommand extends AbstractCommand {
    public ReactionRoleCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Adds and removes roles from users based on reactions from a message\n\n" +
                "You save messages/reactions to keys to make maintaining them a little easier.";
    }

    @Override
    public String getCommand() {
        return "reactionrole";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "rr //overview of all the configured keys",
                "rr add <key> <emote> <role> //adds a reaction with role to the message",
                "rr remove <key> <emote>     //removes emote reaction from key",
                "rr delete <key>             //deletes the set",
                "rr message <key> <message>  //updates the message",
                "rr display <key> [channel]  //displays the message in this channel",
                "                            //or in the channel you specified"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "rr"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        SimpleRank rank = bot.security.getSimpleRank(author);
        channel.getType();
        TextChannel t = (TextChannel) channel;
        if (!PermissionUtil.checkPermission(t.getGuild().getSelfMember(), Permission.MANAGE_ROLES)) {
            return Template.get("i_require_manage_roles");
        }
        if(args.length == 0){
            return "overview goes here TODO";
        }
        if (args.length > 0) {
            if(Misc.parseLong(args[0],0) > 0L){
                Emote emote = bot.getJda().getEmoteById(args[0]);
                if(emote != null){
                    return emote.getAsMention();
                }

            }

            List<Emote> emotes = bot.getJda().getEmotesByName(args[0], true);
            for (Emote emote : emotes) {
                channel.sendMessage(emote.getId()).queue();
            }
            if (emotes.isEmpty()) {
                return "no emotes found for " + args[0];
            }
            else{
                return "";
            }
        }
        switch (args[0].toLowerCase()){
            case "add":// eg. !rr add <key> <emote> <role>
                if(args.length < 3){

                }
                break;
            case "remove"://eg. !rr remove key <emote>
                break;
            case "message":
            case "text"://eg. !rr message key <newtext>
                break;
            case "display"://spams the message here

        }

        return Template.get("command_no_permission");
    }
}