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

package discordbot.handler;

import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CRank;
import discordbot.db.controllers.CUser;
import discordbot.db.controllers.CUserRank;
import discordbot.db.model.OGuild;
import discordbot.db.model.OUserRank;
import discordbot.main.Config;
import discordbot.main.GuildCheckResult;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages permissions/bans for discord
 */
public class SecurityHandler {
    private static HashSet<Long> bannedGuilds;
    private static HashSet<Long> bannedUsers;
    private static HashSet<Long> interactionBots;
    private static HashSet<Long> contributors;
    private static HashSet<Long> botAdmins;
    private static HashSet<Long> systemAdmins;

    public SecurityHandler() {
    }

    public static synchronized void initialize() {
        bannedGuilds = new HashSet<>();
        bannedUsers = new HashSet<>();
        interactionBots = new HashSet<>();
        contributors = new HashSet<>();
        botAdmins = new HashSet<>();
        systemAdmins = new HashSet<>();
        List<OGuild> bannedList = CGuild.getBannedGuilds();
        bannedGuilds.addAll(bannedList.stream().map(guild -> guild.discord_id).collect(Collectors.toList()));
        CUser.addBannedUserIds(bannedUsers);

        List<OUserRank> interaction_bots = CUserRank.getUsersWith(CRank.findBy("UNTERACTION_BOT").id);
        List<OUserRank> contributor = CUserRank.getUsersWith(CRank.findBy("CONTRIBUTOR").id);
        List<OUserRank> bot_admin = CUserRank.getUsersWith(CRank.findBy("BOT_ADMIN").id);
        List<OUserRank> system_admin = CUserRank.getUsersWith(CRank.findBy("SYSTEM_ADMIN").id);
        contributors.addAll(contributor.stream().map(oUserRank -> Long.parseLong(CUser.getCachedDiscordId(oUserRank.userId))).collect(Collectors.toList()));
        interactionBots.addAll(interaction_bots.stream().map(oUserRank -> Long.parseLong(CUser.getCachedDiscordId(oUserRank.userId))).collect(Collectors.toList()));
        botAdmins.addAll(bot_admin.stream().map(oUserRank -> Long.parseLong(CUser.getCachedDiscordId(oUserRank.userId))).collect(Collectors.toList()));
        systemAdmins.addAll(system_admin.stream().map(oUserRank -> Long.parseLong(CUser.getCachedDiscordId(oUserRank.userId))).collect(Collectors.toList()));
    }

    public boolean isBanned(User user) {
        return bannedUsers.contains(Long.parseLong(user.getId()));
    }

    public synchronized void addUserBan(long discordId) {
        if (!bannedUsers.contains(discordId)) {
            bannedUsers.add(discordId);
        }
    }

    public synchronized void removeUserBan(long discordId) {
        if (bannedUsers.contains(discordId)) {
            bannedUsers.remove(discordId);
        }
    }

    public boolean isBanned(Guild guild) {
        return bannedGuilds.contains(Long.parseLong(guild.getId()));
    }

    public SimpleRank getSimpleRank(User user) {
        return getSimpleRankForGuild(user, null);
    }

    public SimpleRank getSimpleRank(User user, MessageChannel channel) {
        if (channel instanceof TextChannel) {
            return getSimpleRankForGuild(user, ((TextChannel) channel).getGuild());
        }
        return getSimpleRankForGuild(user, null);
    }

    /**
     * Try and figure out what type of guild it is
     *
     * @param guild the guild to check
     * @return what category the guild is labeled as
     */
    public GuildCheckResult checkGuild(Guild guild) {

        int bots = 0;
        int users = 0;
        if (MiscUtil.getCreationTime(guild.getOwner().getUser()).isBefore(OffsetDateTime.now().minusDays(Config.GUILD_OWNER_MIN_ACCOUNT_AGE))) {
            return GuildCheckResult.OWNER_TOO_NEW;
        }
        for (Member user : guild.getMembers()) {
            if (user.getUser().isBot()) {
                bots++;
            }
            users++;
        }
        if ((double) bots / users > Config.GUILD_MAX_USER_BOT_RATIO) {
            return GuildCheckResult.BOT_GUILD;
        }
        if (users < Config.GUILD_MIN_USERS) {
            return GuildCheckResult.TEST_GUILD;
        }
        return GuildCheckResult.OKE;
    }

    public SimpleRank getSimpleRankForGuild(User user, Guild guild) {
        long userId = Long.parseLong(user.getId());
        if (user.getId().equals(Config.CREATOR_ID)) {
            return SimpleRank.CREATOR;
        }
        if (botAdmins.contains(userId)) {
            return SimpleRank.BOT_ADMIN;
        }
        if (contributors.contains(userId)) {
            return SimpleRank.CONTRIBUTOR;
        }
        if (bannedUsers.contains(userId)) {
            return SimpleRank.BANNED_USER;
        }
        if (systemAdmins.contains(userId)) {
            return SimpleRank.SYSTEM_ADMIN;
        }
        if (guild != null) {
            if (guild.getOwner().equals(user)) {
                return SimpleRank.GUILD_OWNER;
            }
            if (PermissionUtil.checkPermission(guild, guild.getMember(user), Permission.ADMINISTRATOR)) {
                return SimpleRank.GUILD_ADMIN;
            }
        }
        if (user.isBot()) {
            if (interactionBots.contains(userId)){
                return SimpleRank.INTERACTION_BOT;
            }
            return SimpleRank.BOT;
        }
        return SimpleRank.USER;
    }

    public boolean isBotAdmin(long discordUserId) {
        return botAdmins.contains(discordUserId);
    }
}
