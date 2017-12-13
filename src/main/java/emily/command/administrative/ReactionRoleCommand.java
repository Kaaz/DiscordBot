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
import emily.db.controllers.CReactionRoleKey;
import emily.db.model.OReactionRoleKey;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        SimpleRank rank = bot.security.getSimpleRank(author);
        TextChannel t = (TextChannel) channel;
        if (!PermissionUtil.checkPermission(t.getGuild().getSelfMember(), Permission.MANAGE_ROLES)) {
            return Template.get("i_require_manage_roles");
        }
        if (args.length == 0) {
            List<OReactionRoleKey> list = CReactionRoleKey.getKeysForGuild(t.getGuild().getId());
            String result = "";
            if (list.isEmpty()) {
                return "No keys are configured";
            }
            for (OReactionRoleKey key : list) {
                result += key.messageKey + "\n";
            }
            return "all configured keys: \n" + result;
        }
        switch (args[0].toLowerCase()) {
            case "add":// eg. !rr add <key> <emote> <role>
                if (args.length >= 4) {
                    Role role = DisUtil.findRole(t.getGuild(), args[3]);
                    OReactionRoleKey key = CReactionRoleKey.findOrCreate(t.getGuild().getId(), args[1]);
                    if (!isEmote(bot, args[2])) {
                        return "no emote found";
                    }
                    if (role == null) {
                        return "no role found containing `" + args[3] + "`";
                    }
                    return String.format("adding to key `%s` the reaction %s with role `%s`", args[1], toDisplay(bot, args[2]), role.getName());
                }
                return "invalid usage! see help for more info";
            case "remove"://eg. !rr remove key <emote>
                return "invalid usage! see help for more info";
            case "message":
            case "text"://eg. !rr message key <newtext>
                return "invalid usage! see help for more info";
            case "display"://spams the message here
                if (args.length < 2) {
                    return "invalid usage! see help for more info";
                }
                OReactionRoleKey key = CReactionRoleKey.findBy(t.getGuild().getId(), args[1]);
                if (key.id == 0) {
                    return String.format("key `%s` not found!", args[1]);
                }
                displayMessage(t, key);
                return "";

        }

        return Template.get("command_no_permission");
    }

    private void displayMessage(TextChannel channel, OReactionRoleKey key) {
        if (key.channelId > 0 && key.messageId > 0) {
            TextChannel tchan = channel.getGuild().getTextChannelById(key.channelId);
            if (tchan != null && tchan.canTalk()) {
                tchan.deleteMessageById(key.messageId).queue();
            }
        }
        String msg = key.message;
        msg += "\n Use the reactions to give/remove the role";
        channel.sendMessage(msg).queue(message -> {
            key.messageId = message.getIdLong();
            key.channelId = channel.getIdLong();
            CReactionRoleKey.update(key);
            message.addReaction(Emojibet.THUMBS_UP).queue();
            message.addReaction(Emojibet.THUMBS_DOWN).queue();
        });
    }

    public static boolean isEmote(DiscordBot bot, String emote) {
        return EmojiUtils.isEmoji(emote) || Misc.isGuildEmote(emote) || bot.getJda().getEmoteById(emote) != null;
    }

    public static String toDisplay(DiscordBot bot, String emote) {
        if (EmojiUtils.isEmoji(emote)) {
            return emote;
        } else if (Misc.isGuildEmote(emote)) {
            return bot.getJda().getEmoteById(Misc.getGuildEmoteId(emote)).getAsMention();
        } else if (bot.getJda().getEmoteById(emote) != null) {
            return bot.getJda().getEmoteById(emote).getAsMention();
        }
        return "";
    }
}