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

package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBanks;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CGuildMember;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OBank;
import discordbot.db.model.OGuild;
import discordbot.db.model.OGuildMember;
import discordbot.db.model.OUser;
import discordbot.guildsettings.bot.SettingUseEconomy;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Misc;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * !user
 * shows some info about the user
 */
public class UserCommand extends AbstractCommand {
    private final SimpleDateFormat joindateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public UserCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Shows information about the user";
    }

    @Override
    public String getCommand() {
        return "user";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "user                             //info about you",
                "user @user                       //info about @user",
                "user @user joindate yyyy-MM-dd   //overrides the join-date of a user",
                "user @user joindate reset        //restores the original value",
                "user guilds @user                //what guilds/shards @user most likely uses"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "whois"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        User infoUser = null;
        if (args.length == 0) {
            infoUser = author;
        } else if (DisUtil.isUserMention(args[0])) {
            infoUser = channel.getJDA().getUserById(DisUtil.mentionToId(args[0]));
        } else if (args[0].matches("i\\d+")) {
            OUser dbUser = CUser.findById(Integer.parseInt(args[0].substring(1)));
            infoUser = channel.getJDA().getUserById(dbUser.discord_id);
        } else if (channel instanceof TextChannel) {
            if (args.length >= 2 && args[0].equals("guilds") && bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
                System.out.println(Misc.joinStrings(args,1));
                User user = DisUtil.findUser((TextChannel) channel, Misc.joinStrings(args, 1));
                if (user == null) {
                    return Template.get("command_user_not_found");
                }
                List<OGuild> guilds = CGuild.getMostUsedGuildsFor(CUser.getCachedId(author.getId()));
                List<List<String>> tbl = new ArrayList<>();
                for (OGuild guild : guilds) {
                    tbl.add(Arrays.asList("" + bot.getContainer().calcShardId(guild.discord_id), Long.toString(guild.discord_id), guild.name));
                }
                return Misc.makeAsciiTable(Arrays.asList("shard", "guild", "name"), tbl, null);
            }
            Member member = DisUtil.findUserIn((TextChannel) channel, args[0]);
            if (member != null) {
                infoUser = member.getUser();
            }
        }
        if (infoUser == null) {
            return Template.get("command_user_not_found");
        }

        int userId = CUser.getCachedId(infoUser.getId(), infoUser.getName());
        int guildId = 0;

        String nickname = infoUser.getName();
        if (channel instanceof TextChannel) {
            guildId = CGuild.getCachedId(((TextChannel) channel).getGuild().getId());
            nickname = ((TextChannel) channel).getGuild().getMember(infoUser).getEffectiveName();
        }
        if (args.length >= 3 && guildId > 0) {
            if (args[1].equals("joindate")) {
                try {
                    OGuildMember member = CGuildMember.findBy(guildId, userId);
                    Guild guild = ((TextChannel) channel).getGuild();
                    if (args[2].equals("reset")) {
                        member.joinDate = new Timestamp(guild.getMember(infoUser).getJoinDate().toInstant().toEpochMilli());
                    } else {
                        member.joinDate = new Timestamp(joindateFormat.parse(args[2].replace("-", "/")).getTime());
                    }
                    CGuildMember.insertOrUpdate(member);
                    return Template.get("command_user_joindate_set", infoUser.getName(), joindateFormat.format(member.joinDate));
                } catch (ParseException e) {
                    return Template.get("command_invalid_use");
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        OUser dbUser = CUser.findBy(infoUser.getId());
        sb.append("Querying for ").append(nickname).append(Config.EOL);
        sb.append(":bust_in_silhouette: User: ").append(infoUser.getName()).append("#").append(infoUser.getDiscriminator()).append(Config.EOL);
        sb.append(":id: discord id:").append(infoUser.getId()).append(Config.EOL);
        sb.append(":keyboard: Commands used:").append(dbUser.commandsUsed).append(Config.EOL);
        if (guildId == 0 || "true".equals(GuildSettings.getFor(channel, SettingUseEconomy.class))) {
            OBank bankAccount = CBanks.findBy(userId);
            sb.append(Config.ECONOMY_CURRENCY_ICON).append(" ").append(Config.ECONOMY_CURRENCY_NAMES).append(": ").append(bankAccount.currentBalance).append(Config.EOL);
        }

        if (guildId > 0) {
            Guild guild = ((TextChannel) channel).getGuild();
            OGuildMember member = CGuildMember.findBy(guildId, userId);
            if (member.joinDate == null) {
                member.joinDate = new Timestamp(guild.getMember(infoUser).getJoinDate().toInstant().toEpochMilli());
                CGuildMember.insertOrUpdate(member);
            }

            sb.append(":date: joined: ")
                    .append(joindateFormat.format(member.joinDate))
                    .append(" (")
                    .append(TimeUtil.getRelativeTime(member.joinDate.getTime() / 1000L, false, true))
                    .append(")")
                    .append(Config.EOL);

        }
        if (infoUser.getAvatarUrl() != null) {
            sb.append(":frame_photo: Avatar: <").append(infoUser.getAvatarUrl()).append(">").append(Config.EOL);
        }
        if (infoUser.isBot()) {
            sb.append(":robot: This user is a bot (or pretends to be)");
        }
        return sb.toString();

    }
}