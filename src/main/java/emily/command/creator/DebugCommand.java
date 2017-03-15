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

package emily.command.creator;

import emily.core.AbstractCommand;
import emily.db.controllers.CGuild;
import emily.db.controllers.CGuildMember;
import emily.db.controllers.CUser;
import emily.db.model.OGuildMember;
import emily.handler.Template;
import emily.main.Config;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.Timestamp;

/**
 */
public class DebugCommand extends AbstractCommand {
    public DebugCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "some debugging tools";
    }

    @Override
    public String getCommand() {
        return "debug";
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "fixusernames, fixrelations, youtube ",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        if (!bot.security.getSimpleRank(author).isAtLeast(SimpleRank.CREATOR)) {
            return Template.get(channel, "command_no_permission");
        }
        if (args.length == 0) {
            return Emojibet.EYES;
        }
        boolean value = false;
        boolean updating = args.length > 1;
        if (updating) {
            value = Misc.isFuzzyTrue(args[1]);
        }
        switch (args[0].toLowerCase()) {
            case "fixusernames":
                fixUserNames(bot, channel);
                return "";
            case "fixrelations":
                fixMemberships(bot, channel);
                return "";
            case "yt":
            case "youtube":
                if (updating) {
                    Config.YOUTUBEDL_DEBUG_PROCESS = value;
                }
                value = Config.YOUTUBEDL_DEBUG_PROCESS;
                break;
            default:
                return Emojibet.SHRUG;
        }
        if (updating) {
            return Emojibet.OKE_SIGN + " " + args[0] + " is set to " + value;
        }
        return Emojibet.UNLOCKED + " " + args[0] + " = `" + value + "`";
    }

    private void fixUserNames(DiscordBot bot, MessageChannel channel) {
        DiscordBot[] shards = bot.getContainer().getShards();
        long tmp = 0;
        final long updateInterval = 2500;
        for (DiscordBot shard : shards) {
            tmp += shard.getJda().getUsers().size();
        }
        final long totalUsers = tmp;
        int length = 1 + (int) Math.floor(Math.log10(totalUsers));
        String messageFormat = Emojibet.INFORMATION + " Synchronized " + Emojibet.USER + " %0" + length + "d / %0" + length + "d |  %.2f%%";
        channel.sendMessage(Emojibet.INFORMATION + " Synchronizing names: " + totalUsers + " users").queue(message -> {
                    long usersCompleted = 0;
                    for (DiscordBot shard : shards) {
                        for (User user : shard.getJda().getUsers()) {
                            CUser.getCachedId(user.getId(), user.getName());
                            usersCompleted++;
                            if (usersCompleted % updateInterval == 0L) {
                                message.editMessage(String.format(messageFormat, usersCompleted, totalUsers, (double) usersCompleted / (double) totalUsers * 100D)).queue();
                            }
                        }
                    }
                    if (usersCompleted % updateInterval > 0) {
                        message.editMessage(String.format(messageFormat, usersCompleted, totalUsers, (double) usersCompleted / (double) totalUsers * 100D)).queue();
                    }
                }
        );
    }

    private void fixMemberships(DiscordBot bot, MessageChannel channel) {
        DiscordBot[] shards = bot.getContainer().getShards();
        long tmpMembers = 0, tmpGuilds = 0;
        final long updateInterval = 2500;
        for (DiscordBot shard : shards) {
            tmpGuilds += shard.getJda().getGuilds().size();
            for (Guild guild : shard.getJda().getGuilds()) {
                tmpMembers += guild.getMembers().size();
            }
        }
        final long totalUsers = tmpMembers;
        final long totalGuilds = tmpGuilds;
        int length = 1 + (int) Math.floor(Math.log10(totalUsers));
        int guildLength = 1 + (int) Math.floor(Math.log10(totalGuilds));
        String messageFormat = Emojibet.INFORMATION + " Synchronized " +
                Emojibet.GUILD_JOIN + " %0" + guildLength + "d / %0" + guildLength + "d | "
                + Emojibet.USER + " %0" + length + "d / %0" + length + "d |  %.2f%%";
        channel.sendMessage(Emojibet.INFORMATION + " Synchronizing memberships: " + totalUsers + " users").queue(message -> {
                    long usersCompleted = 0;
                    long guildsCompleted = 0;
                    for (DiscordBot shard : shards) {
                        for (Guild guild : shard.getJda().getGuilds()) {
                            for (Member member : guild.getMembers()) {
                                User guildUser = member.getUser();
                                int userId = CUser.getCachedId(guildUser.getId(), guildUser.getName());
                                OGuildMember guildMember = CGuildMember.findBy(CGuild.getCachedId(Long.parseLong(guild.getId())), userId);
                                guildMember.joinDate = new Timestamp(member.getJoinDate().toInstant().toEpochMilli());
                                CGuildMember.insertOrUpdate(guildMember);
                                usersCompleted++;
                                if (usersCompleted % updateInterval == 0L) {
                                    message.editMessage(String.format(messageFormat,
                                            guildsCompleted, totalGuilds,
                                            usersCompleted, totalUsers,
                                            (float) usersCompleted / (float) totalUsers * 100F
                                    )).queue();
                                }
                            }
                            guildsCompleted++;
                        }
                    }
                    message.editMessage(String.format(messageFormat,
                            guildsCompleted, totalGuilds,
                            usersCompleted, totalUsers,
                            (float) usersCompleted / (float) totalUsers * 100F
                    )).queue();
                }
        );
    }
}