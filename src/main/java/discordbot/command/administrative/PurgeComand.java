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

package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Misc;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.core.MessageHistory;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * !purge
 * Purges messages in channel
 */
public class PurgeComand extends AbstractCommand {
    private static final int MAX_DELETE_COUNT = 2500;
    private static final int MAX_BULK_SIZE = 100;

    public PurgeComand() {
        super();
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String getDescription() {
        return "deletes non-pinned messages";
    }

    @Override
    public String getCommand() {
        return "purge";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "//deletes up to " + MAX_BULK_SIZE + " non-pinned messages",
                "purge",
                "//deletes <limit> (max " + MAX_DELETE_COUNT + ") non-pinned messages",
                "purge <limit>",
                "//deletes messages newer than now - (input)",
                "purge time 1d2h10m         //you can use dhms and combinations ",
                "//deletes <limit> messages from <user>, limit is optional",
                "purge @user [limit]",
                "//deletes messages from <user>, user can be part of a user's name",
                "purge user <user>",
                "//deletes messages matching <regex>",
                "purge matches <regex>",
                "//delete messages NOT matching <regex>",
                "purge notmatches <regex>",
                "//delete command related messages",
                "purge commands",
                "//deletes bot messages",
                "purge bot"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "clear",
                "delete"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        Guild guild = ((TextChannel) channel).getGuild();
        final boolean hasManageMessages = PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_MANAGE);
        List<Message> messagesToDelete = new ArrayList<>();
        Member toDeleteFrom = null;
        Pattern deletePattern = null;
        long maxMessageAge = TimeUnit.DAYS.toMillis(14);
        int toDelete = 100;
        final String cmdPrefix = DisUtil.getCommandPrefix(channel);
        PurgeStyle style = PurgeStyle.UNKNOWN;
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN) && !channel.getJDA().getSelfUser().equals(author)) {
            return Template.get("no_permission");
        }
        if (args.length == 0) {
            style = PurgeStyle.ALL;
        }
        if (args.length > 0) {
            switch (args[0]) {
                case "time":
                    if (args.length > 1) {
                        style = PurgeStyle.ALL;
                        maxMessageAge = Math.min(maxMessageAge, TimeUtil.toMillis(Misc.joinStrings(args, 1)));
                    }
                    break;
                case "commands":
                case "command":
                    style = PurgeStyle.COMMANDS;
                    break;
                case "bot":
                case "bots":
                case "emily":
                    style = PurgeStyle.BOTS;
                    break;
                case "user":
                    style = PurgeStyle.AUTHOR;
                    if (args.length > 1) {
                        User user = DisUtil.findUser((TextChannel) channel, Misc.joinStrings(args, 1));
                        if (user != null) {
                            toDeleteFrom = guild.getMember(user);
                        }
                        if (toDeleteFrom != null && author.getId().equals(toDeleteFrom.getUser().getId())) {
                            toDelete++;//exclude the command itself from the limit
                        }
                        if (toDeleteFrom != null && !hasManageMessages && !channel.getJDA().getSelfUser().getId().equals(toDeleteFrom.getUser().getId())) {
                            return Template.get("permission_missing_manage_messages");
                        }
                    }
                    break;
                case "matches":
                    style = PurgeStyle.MATCHES;
                case "notmatches":
                    if (style.equals(PurgeStyle.UNKNOWN)) {
                        style = PurgeStyle.NOTMATCHES;
                    }
                    String regex = Misc.joinStrings(args, 1);
                    try {
                        deletePattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    } catch (PatternSyntaxException exception) {
                        return Template.get("command_autoreply_regex_invalid") + Config.EOL +
                                exception.getDescription() + Config.EOL +
                                Misc.makeTable(exception.getMessage());
                    }
            }
        }
        if (style.equals(PurgeStyle.UNKNOWN)) {
            if (DisUtil.isUserMention(args[0])) {
                toDeleteFrom = guild.getMember(channel.getJDA().getUserById(DisUtil.mentionToId(args[0])));
                if (args.length >= 2 && args[1].matches("^\\d+$")) {
                    toDelete = Math.min(MAX_DELETE_COUNT, Integer.parseInt(args[1]));
                }
            } else if (args[0].matches("^\\d+$")) {
                toDelete = Math.min(MAX_DELETE_COUNT, Misc.parseInt(args[0], toDelete)) + 1;
            } else {
                toDeleteFrom = DisUtil.findUserIn((TextChannel) channel, args[0]);
                if (args.length >= 2 && args[1].matches("^\\d+$")) {
                    toDelete = Math.min(toDelete, Integer.parseInt(args[1])) + 1;
                }
            }
        }
        int finalDeleteLimit = toDelete;
        long twoWeeksAgo = (System.currentTimeMillis() - maxMessageAge - MiscUtil.DISCORD_EPOCH) << MiscUtil.TIMESTAMP_OFFSET;
        Member finalToDeleteFrom = toDeleteFrom;
        PurgeStyle finalStyle = style;
        Pattern finalDeletePattern = deletePattern;
        int totalMessages = toDelete;
        MessageHistory history = channel.getHistory();
        int deletedCount = 0;
        boolean oldMessageDetected = false;
        do {
            int part = Math.min(MAX_BULK_SIZE, totalMessages);
            List<Message> messages = history.retrievePast(part).complete();
            if (messages.isEmpty()) {
                break;
            }
            for (Message msg : messages) {
                if (deletedCount == finalDeleteLimit) {
                    break;
                }
                if (msg.isPinned() || Long.parseLong(msg.getId()) < twoWeeksAgo) {
                    oldMessageDetected = true;
                    break;
                }
                switch (finalStyle) {
                    case ALL:
                        if ((hasManageMessages
                                || (msg.getAuthor() != null && msg.getAuthor().getId().equals(msg.getJDA().getSelfUser().getId())))) {
                            messagesToDelete.add(msg);
                            deletedCount++;
                        }
                        break;
                    case BOTS:
                        if ((hasManageMessages && msg.getAuthor() != null && msg.getAuthor().isBot())
                                || msg.getAuthor() != null && msg.getAuthor().isBot()) {
                            messagesToDelete.add(msg);
                            deletedCount++;
                        }
                        break;
                    case AUTHOR:
                        if (finalToDeleteFrom != null && msg.getAuthor() != null && msg.getAuthor().getId().equals(finalToDeleteFrom.getUser().getId())) {
                            messagesToDelete.add(msg);
                            deletedCount++;
                        }
                        break;
                    case COMMANDS:
                        if ((msg.getRawContent().startsWith(cmdPrefix) && hasManageMessages)
                                || (msg.getAuthor() == null || msg.getAuthor().getId().equals(msg.getJDA().getSelfUser().getId()))) {
                            messagesToDelete.add(msg);
                            deletedCount++;
                        }
                        break;
                    case MATCHES:
                        if (hasManageMessages && finalDeletePattern.matcher(msg.getRawContent()).find()) {
                            messagesToDelete.add(msg);
                            deletedCount++;
                        }
                        break;
                    case NOTMATCHES:
                        if (hasManageMessages && !finalDeletePattern.matcher(msg.getRawContent()).find()) {
                            messagesToDelete.add(msg);
                            deletedCount++;
                        }
                        break;
                    case UNKNOWN:
                        messagesToDelete.add(msg);
                        deletedCount++;

                }
            }
            totalMessages -= part;
            if (oldMessageDetected) {
                break;
            }
        } while (totalMessages > 0);
        deleteBulk(bot, (TextChannel) channel, hasManageMessages, messagesToDelete);
        return "";
    }

    /**
     * Deletes a bunch of messages
     *
     * @param bot               the jda instance
     * @param channel           channel to delete messages in
     * @param hasManageMessages does the bot have the Permission.MANAGE_CHANNEL for channel
     * @param messagesToDelete  list of messages to delete
     */
    private void deleteBulk(DiscordBot bot, TextChannel channel, boolean hasManageMessages, List<Message> messagesToDelete) {
        if (messagesToDelete.isEmpty()) {
            return;
        }
        if (hasManageMessages) {
            bot.out.sendAsyncMessage(channel, Template.get(
                    "command_purge_success", messagesToDelete.size()), message -> {
                messagesToDelete.add(message);
                for (int index = 0; index < messagesToDelete.size(); index += MAX_BULK_SIZE) {
                    if (messagesToDelete.size() - index < 2) {
                        messagesToDelete.get(index).deleteMessage().queue();
                    } else {
                        channel.deleteMessages(messagesToDelete.subList(index, Math.min(index + MAX_BULK_SIZE, messagesToDelete.size()))).queue();
                    }
                    try {
                        Thread.sleep(2000L);
                    } catch (Exception ignored) {
                    }
                }
            });
        } else {
            bot.out.sendAsyncMessage(channel, Template.get("permission_missing_manage_messages"), message -> {
                messagesToDelete.add(message);
                for (Message toDelete : messagesToDelete) {
                    if (toDelete.getAuthor().getId().equals(channel.getJDA().getSelfUser().getId()))
                        toDelete.deleteMessage().queue();
                    try {
                        Thread.sleep(500L);
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    private enum PurgeStyle {
        UNKNOWN, ALL, BOTS, AUTHOR, MATCHES, NOTMATCHES, COMMANDS
    }
}