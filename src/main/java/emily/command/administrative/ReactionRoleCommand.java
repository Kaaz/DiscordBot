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
import emily.db.controllers.CReactionRole;
import emily.db.model.OReactionRoleKey;
import emily.db.model.OReactionRoleMessage;
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
            List<OReactionRoleKey> list = CReactionRole.getKeysForGuild(t.getGuild().getId());
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
                    OReactionRoleKey key = CReactionRole.findOrCreate(t.getGuild().getId(), args[1]);
                    if (!isEmote(bot, args[2])) {
                        return "no emote found";
                    }
                    if (role == null) {
                        return "no role found containing `" + args[3] + "`";
                    }
                    boolean isNormalEmote = EmojiUtils.isEmoji(args[2]);
                    String emoteId = Misc.getGuildEmoteId(args[2]);
                    if (!isNormalEmote && bot.getJda().getEmoteById(emoteId) == null) {
                        return "can't find guild-emote";
                    }
                    CReactionRole.addReaction(key.id, isNormalEmote ? args[2] : emoteId, isNormalEmote, role.getIdLong());
                    return String.format("adding to key `%s` the reaction %s with role `%s`", args[1], toDisplay(bot, args[2]), role.getName());
                }
                return "invalid usage! see help for more info";
            case "remove"://eg. !rr remove key <emote>
                return "invalid usage! see help for more info";
            case "message":
            case "msg":
            case "text"://eg. !rr message key <newtext>
                if (args.length >= 2) {
                    OReactionRoleKey key = CReactionRole.findBy(t.getGuild().getId(), args[1]);
                    if (key.id == 0) {
                        return String.format("key `%s` doesn't exist", args[1]);
                    }
                    key.message = Misc.joinStrings(args, 2);
                    if (key.message.length() > 1500) {
                        key.message = key.message.substring(0, 1500);
                    }
                    CReactionRole.update(key);
                    updateText(t, key);
                    return String.format("Text for %s updated!", args[1]);
                }
                return "invalid usage! see help for more info";
            case "display"://spams the message here
                if (args.length < 2) {
                    return "invalid usage! see help for more info";
                }
                OReactionRoleKey key = CReactionRole.findBy(t.getGuild().getId(), args[1]);
                if (key.id == 0) {
                    return String.format("key `%s` not found!", args[1]);
                }
                displayMessage(bot, t, key);
                return "";

        }

        return Template.get("command_no_permission");
    }

    private void updateText(TextChannel channel, OReactionRoleKey key) {
        if (key.messageId > 0 && key.channelId > 0) {
            TextChannel tchan = channel.getGuild().getTextChannelById(key.channelId);
            if (tchan != null && tchan.canTalk()) {
                tchan.editMessageById(String.valueOf(key.messageId), key.message + "\n Use the reactions to give/remove the role").queue();
            }
        }
    }

    private void displayMessage(DiscordBot bot, TextChannel channel, OReactionRoleKey key) {
        if (key.channelId > 0 && key.messageId > 0) {
            TextChannel tchan = channel.getGuild().getTextChannelById(key.channelId);
            if (tchan != null && tchan.canTalk()) {
                tchan.deleteMessageById(key.messageId).queue();
            }
        }
        String msg = key.message;
        msg += "\n Use the reactions to give/remove the role\n";
        List<OReactionRoleMessage> reactions = CReactionRole.getReactionsForKey(key.id);
        for (OReactionRoleMessage reaction : reactions) {
            msg += String.format("%s %s %s\n",
                    reaction.isNormalEmote ? reaction.emoji : channel.getJDA().getEmoteById(reaction.emoji),
                    Emojibet.THUMBS_RIGHT,
                    channel.getGuild().getRoleById(reaction.roleId));
        }
        channel.sendMessage(msg).queue(message -> {
            key.messageId = message.getIdLong();
            key.channelId = channel.getIdLong();
            CReactionRole.update(key);
            bot.roleReactionHandler.initGuild(message.getGuild().getIdLong(), true);
            for (OReactionRoleMessage reaction : reactions) {
                if (reaction.isNormalEmote) {
                    message.addReaction(reaction.emoji).queue();
                } else {
                    message.addReaction(message.getJDA().getEmoteById(reaction.emoji)).queue();
                }
            }
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