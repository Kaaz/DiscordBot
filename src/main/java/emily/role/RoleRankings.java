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

package emily.role;

import emily.db.controllers.CGuildMember;
import emily.db.model.OGuildMember;
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.main.DiscordBot;
import emily.main.Launcher;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 19-9-2016
 */
public class RoleRankings {

    private static final ArrayList<MemberShipRole> roles = new ArrayList<>();
    private static final Set<String> roleNames = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleRankings.class);
    private volatile static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        //this may or may not be based on the ph scale
        roles.add(new MemberShipRole("Spectator", new Color(0xA700FF), 0));
        roles.add(new MemberShipRole("Outsider", new Color(0x8E00F9), TimeUnit.HOURS.toMillis(1L)));
        roles.add(new MemberShipRole("Lurker", new Color(0x8140FF), TimeUnit.HOURS.toMillis(4L)));
        roles.add(new MemberShipRole("Neutral", new Color(0x664AEF), TimeUnit.DAYS.toMillis(1L)));
        roles.add(new MemberShipRole("Prospect", new Color(0x413FE9), TimeUnit.DAYS.toMillis(2L)));
        roles.add(new MemberShipRole("Friendly", new Color(0x3E69E4), TimeUnit.DAYS.toMillis(4L)));
        roles.add(new MemberShipRole("Regular", new Color(0x2F74DF), TimeUnit.DAYS.toMillis(7L)));
        roles.add(new MemberShipRole("Honored", new Color(0xFADED), TimeUnit.DAYS.toMillis(14L)));
        roles.add(new MemberShipRole("Veteran", new Color(0x35B6D4), TimeUnit.DAYS.toMillis(28L)));
        roles.add(new MemberShipRole("Revered", new Color(0x0DC3CF), TimeUnit.DAYS.toMillis(50L)));
        roles.add(new MemberShipRole("Herald", new Color(0x0ECAB4), TimeUnit.DAYS.toMillis(75L)));
        roles.add(new MemberShipRole("Exalted", new Color(0x0FC490), TimeUnit.DAYS.toMillis(125L), true));
        roles.add(new MemberShipRole("Beloved", new Color(0x10BF6D), TimeUnit.DAYS.toMillis(200L), true));
        roles.add(new MemberShipRole("Favorite", new Color(0x11BA4D), TimeUnit.DAYS.toMillis(365L), true));
        roles.add(new MemberShipRole("Consul", new Color(0x11B52F), TimeUnit.DAYS.toMillis(500L), true));
        for (MemberShipRole role : roles) {
            roleNames.add(role.getName().toLowerCase());
        }
    }

    /**
     * retrieves a list of all membership roles
     *
     * @return list
     */
    public static List<MemberShipRole> getAllRoles() {
        return roles;
    }

    /**
     * Prefixes the role name based on the guild's setting
     *
     * @param guild the guild
     * @param role  the role
     * @return full name
     */
    public static String getFullName(Guild guild, MemberShipRole role) {
        return getPrefix(guild) + " " + role.getName();
    }

    public static MemberShipRole getHighestRole(Long memberLengthInMilis) {
        for (int i = roles.size() - 1; i >= 0; i--) {
            if (roles.get(i).getMembershipTime() <= memberLengthInMilis) {
                return roles.get(i);
            }
        }
        return roles.get(0);
    }


    /**
     * Attempts to fix create the membership roles for a guild
     *
     * @param guild the guild to create/modify the roles for
     */
    public static void fixForServer(Guild guild) {
        for (int i = roles.size() - 1; i >= 0; i--) {
            fixRole(guild, roles.get(i));
        }
    }

    public static String getPrefix(Guild guild) {
        return GuildSettings.get(guild).getOrDefault(GSetting.USER_TIME_RANKS_PREFIX);
    }

    /**
     * Fixes adds/modifies a membership role to match the settings
     *
     * @param guild the guild to add/modify the role for
     * @param rank  the role to add/modify
     */
    private static void fixRole(Guild guild, MemberShipRole rank) {
        List<Role> rolesByName = guild.getRolesByName(getFullName(guild, rank), true);
        Role role;
        boolean needsUpdate = false;
        if (rolesByName.size() > 0) {
            role = rolesByName.get(0);
        } else {
            Role newRole = guild.getController().createRole().complete();
            RoleManagerUpdatable manager = newRole.getManagerUpdatable();
            manager.getNameField().setValue(getFullName(guild, rank));
            manager.getColorField().setValue(rank.getColor());
            manager.getHoistedField().setValue(rank.isHoisted());
            manager.getPermissionField().setPermissions(guild.getPublicRole().getPermissions());
            manager.getPermissionField().revokePermissions(Permission.MESSAGE_MENTION_EVERYONE);
            manager.update().complete();
            return;
        }
        if (!PermissionUtil.canInteract(guild.getSelfMember(), role)) {
            return;
        }
        if (!role.getName().equals(getFullName(guild, rank))) {
            role.getManagerUpdatable().getNameField().setValue(getFullName(guild, rank));
            needsUpdate = true;
        }
        if (role.getColor() != rank.getColor()) {
            role.getManagerUpdatable().getColorField().setValue(rank.getColor());
            needsUpdate = true;
        }
        if (role.getPermissions().contains(Permission.MESSAGE_MENTION_EVERYONE)) {
            needsUpdate = true;
            role.getManagerUpdatable().getPermissionField().revokePermissions(Permission.MESSAGE_MENTION_EVERYONE);
        }
        if (needsUpdate) {
            role.getManagerUpdatable().update().complete();
        }
    }

    /**
     * checks if a user has the manage roles permission
     *
     * @param guild   the guild to check
     * @param ourUser the user to check for
     * @return has the manage roles premission?
     */
    public static boolean canModifyRoles(Guild guild, User ourUser) {
        return PermissionUtil.checkPermission(guild.getSelfMember(), Permission.MANAGE_ROLES);
    }

    /**
     * deletes the created roles
     *
     * @param guild   the guild to clean up
     * @param ourUser the bot user
     */
    public static void cleanUpRoles(Guild guild, User ourUser) {
        if (!canModifyRoles(guild, ourUser)) {
            return;
        }
        for (Role role : guild.getRoles()) {
            if (role.getName().equals("new role") || role.getName().contains(getPrefix(guild))) {
                role.delete().complete();
            } else if (roleNames.contains(role.getName().toLowerCase())) {
                role.delete().complete();
            }
        }
    }

    /**
     * Attempts to fix create the membership roles for all guilds
     *
     * @param guilds the guilds to fix the roles for
     */
    public static void fixRoles(List<Guild> guilds) {
        for (Guild guild : guilds) {
            try {
                if (GuildSettings.get(guild) != null) {
                    if (!"true".equals(GuildSettings.get(guild).getOrDefault(GSetting.USER_TIME_RANKS))) {
                        continue;
                    }
                    if (canModifyRoles(guild, guild.getJDA().getSelfUser())) {
                        fixForServer(guild);
                    }
                }
            } catch (Exception e) {
                Launcher.logToDiscord(e, "guild", guild.getId(), "gname", guild.getName());
            }
        }
    }

    /**
     * Asigns the right role to a user based on the Roleranking
     *
     * @param guild the guild
     * @param user  the user
     */
    public static void assignUserRole(DiscordBot bot, Guild guild, User user) {
        List<Role> roles = guild.getMember(user).getRoles();
        OGuildMember membership = CGuildMember.findBy(guild.getIdLong(), user.getIdLong());
        boolean hasTargetRole = false;
        String prefix = RoleRankings.getPrefix(guild);
        if (membership.joinDate == null) {
            membership.joinDate = new Timestamp(guild.getMember(user).getJoinDate().toInstant().toEpochMilli());
            CGuildMember.insertOrUpdate(membership);
        }
        MemberShipRole targetRole = RoleRankings.getHighestRole(System.currentTimeMillis() - membership.joinDate.getTime());
        for (Role role : roles) {
            if (role.getName().startsWith(prefix)) {
                if (role.getName().equals(RoleRankings.getFullName(guild, targetRole))) {
                    hasTargetRole = true;
                } else {
                    bot.out.removeRole(user, role);
                }
            }
        }

        if (!hasTargetRole) {
            List<Role> roleList = guild.getRolesByName(RoleRankings.getFullName(guild, targetRole), true);
            if (roleList.size() > 0) {
                bot.out.addRole(user, roleList.get(0));
            }
        }
    }
}