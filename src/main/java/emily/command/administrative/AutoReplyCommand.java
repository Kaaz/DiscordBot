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

import emily.core.AbstractCommand;
import emily.db.controllers.CGuild;
import emily.db.controllers.CReplyPattern;
import emily.db.controllers.CUser;
import emily.db.model.OGuild;
import emily.db.model.OReplyPattern;
import emily.handler.Template;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.Misc;
import emily.util.TimeUtil;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * managing auto replies for the bot
 */
public class AutoReplyCommand extends AbstractCommand {
    public final static int MIN_TAG_LENGTH = 2;

    public AutoReplyCommand() {
        super();
    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public String getDescription() {
        return "regular expression Patterns where the bot auto-replies to. ";
    }

    @Override
    public String getCommand() {
        return "autoreply";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "ar create <tagname>      //creates tag",
                "ar regex <tag> <value>     //edit the regex of a tag",
                "ar response <tag> <value>  //change the response of a reply",
                "ar tag <tag> <value>       //change the tag of a reply",
                "ar cd <tag> <value>        //change the cooldown (millis) of a reply",
                "ar guild <tag> <guildid>   //guild of a tag, 0 for global",
                "ar test <tag> <text>       //test for a match",
                "ar delete <tag>            //deletes a tag",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "ar"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        SimpleRank rank = bot.security.getSimpleRankForGuild(author, guild);
        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Template.get("no_permission");
        }
        if (args.length == 0) {
            List<OReplyPattern> all = CReplyPattern.getAll(CGuild.getCachedId(guild.getId()));
            List<List<String>> tbl = new ArrayList<>();
            for (OReplyPattern replyPattern : all) {
                List<String> row = new ArrayList<>();

                row.add(replyPattern.tag);
                row.add(replyPattern.pattern);
                row.add(TimeUtil.getRelativeTime((System.currentTimeMillis() + replyPattern.cooldown + 1000L) / 1000L, false, false));
                row.add(replyPattern.reply.substring(0, Math.min(40, replyPattern.reply.length())));
                tbl.add(row);
            }
            return "The following All Auto replies information. For details about a specific one use **`ar <tag>`**" + BotConfig.EOL +
                    Misc.makeAsciiTable(Arrays.asList("tag", "trigger", "cooldown", "response"), tbl, null);
        }
        if (args.length >= 2) {
            if (args[1].length() < MIN_TAG_LENGTH) {
                return Template.get("command_autoreply_tag_length", MIN_TAG_LENGTH);
            }
            OReplyPattern replyPattern = CReplyPattern.findBy(args[1]);
            if (args[0].equals("create")) {
                if (replyPattern.id == 0) {
                    replyPattern.tag = args[1];
                    replyPattern.userId = CUser.getCachedId(author.getId(), author.getName());
                    replyPattern.guildId = rank.isAtLeast(SimpleRank.CREATOR) ? 0 : CGuild.getCachedId(guild.getId());
                    replyPattern.cooldown = TimeUnit.MINUTES.toMillis(1);
                    CReplyPattern.insert(replyPattern);
                    return Template.get("command_autoreply_created", args[1]);
                }
                return Template.get("command_autoreply_already_exists", args[1]);
            }
            if (replyPattern.id == 0) {
                return Template.get("command_autoreply_not_exists", args[1]);
            }
            String restOfArgs = "";
            for (int i = 2; i < args.length; i++) {
                restOfArgs += args[i];
                if (i != args.length - 1) {
                    restOfArgs += " ";
                }
            }
            switch (args[0].toLowerCase()) {
                case "delete":
                case "remove":
                case "del":
                    if (rank.isAtLeast(SimpleRank.CREATOR) || (rank.isAtLeast(SimpleRank.GUILD_ADMIN) && CGuild.getCachedId(guild.getId()) == replyPattern.guildId)) {
                        CReplyPattern.delete(replyPattern);
                        bot.reloadAutoReplies();
                    }
                    return Template.get("command_autoreply_deleted", args[1]);
                case "regex":
                case "pattern":
                case "trigger":
                    try {
                        Pattern pattern = Pattern.compile(restOfArgs);//used to see if a patterns is valid, invalid = exception ;)
                        replyPattern.pattern = restOfArgs;
                        CReplyPattern.update(replyPattern);
                    } catch (PatternSyntaxException exception) {
                        return Template.get("command_autoreply_regex_invalid") + BotConfig.EOL +
                                exception.getDescription() + BotConfig.EOL +
                                Misc.makeTable(exception.getMessage());
                    }
                    bot.reloadAutoReplies();
                    return Template.get("command_autoreply_regex_saved");
                case "guild":
                case "gid":
                    if (!rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
                        return Template.get("no_permission");
                    }
                    if (args[2].equalsIgnoreCase("this")) {
                        replyPattern.guildId = CGuild.getCachedId(guild.getId());
                    } else if (!args[2].equals("0")) {
                        OGuild server = CGuild.findBy(args[2]);
                        if (server.id == 0) {
                            return Template.get("command_autoreply_guild_invalid", args[2]);
                        }
                        replyPattern.guildId = server.id;
                    } else {
                        replyPattern.guildId = 0;
                    }
                    CReplyPattern.update(replyPattern);
                    bot.reloadAutoReplies();
                    return Template.get("command_autoreply_guild_saved", args[2]);
                case "response":
                case "reply":
                    replyPattern.reply = EmojiUtils.shortCodify(restOfArgs);
                    CReplyPattern.update(replyPattern);
                    bot.reloadAutoReplies();
                    return Template.get("command_autoreply_response_saved");
                case "tag":
                    replyPattern.tag = args[2];
                    CReplyPattern.update(replyPattern);
                    bot.reloadAutoReplies();
                    return Template.get("command_autoreply_tag_saved");
                case "cd":
                case "cooldown":
                    replyPattern.cooldown = Math.max(TimeUnit.MINUTES.toMillis(1), Long.parseLong(args[2]));
                    CReplyPattern.update(replyPattern);
                    bot.reloadAutoReplies();
                    return Template.get("command_autoreply_cooldown_saved");
                case "test":
                    Pattern pattern = Pattern.compile(replyPattern.pattern);
                    Matcher matcher = pattern.matcher(restOfArgs);
                    if (matcher.find()) {
//						return String.format("`%s` matches `%s`", restOfArgs, replyPattern.pattern);
                        return replyPattern.reply;
                    }
                    return Template.get("command_autoreply_no_match");
                default:
                    return Template.get("invalid_use");
            }
        }
        if (args.length == 1) {
            OReplyPattern replyPattern = CReplyPattern.findBy(args[0]);
            if (replyPattern.id == 0) {
                return Template.get("command_autoreply_not_exists", args[0]);
            }
            List<List<String>> tbl = new ArrayList<>();
            tbl.add(Arrays.asList("created on ", "" + replyPattern.createdOn));
            tbl.add(Arrays.asList("tag", replyPattern.tag));
            tbl.add(Arrays.asList("creator", "" + replyPattern.userId));
            tbl.add(Arrays.asList("guild", "" + replyPattern.guildId));
            tbl.add(Arrays.asList("pattern", "" + replyPattern.pattern));
            tbl.add(Arrays.asList("reply", "" + replyPattern.reply));
            tbl.add(Arrays.asList("cooldown", "" + TimeUtil.getRelativeTime((System.currentTimeMillis() + replyPattern.cooldown + 1000L) / 1000L, false, false)));
            return "Auto reply information for `" + args[0] + "`:" + Misc.makeAsciiTable(Arrays.asList("Property", "Value"), tbl, null);
        }
        return Template.get("invalid_use");
    }
}