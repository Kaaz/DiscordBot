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

package emily.command.informative;

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.db.controllers.CGuild;
import emily.db.controllers.CGuildRoleAssignable;
import emily.db.model.OGuildRoleAssignable;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Misc;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;


/**
 * !getrole
 * gives a role to a user, or takes it away
 */
public class GetroleCommand extends AbstractCommand {

    public GetroleCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "allows users to request a role";
    }

    @Override
    public String getCommand() {
        return "getrole";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "list                //see what roles are available",
                "remove <rolename>   //removes the <rolename> from you",
                "<rolename>          //assign the <rolename> to you ",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        if (!PermissionUtil.checkPermission(guild.getSelfMember(), Permission.MANAGE_ROLES)) {
            return Templates.permission_missing.format(Permission.MANAGE_ROLES.toString());
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            List<OGuildRoleAssignable> roles = CGuildRoleAssignable.getRolesFor(CGuild.getCachedId(guild.getId()));
            if (roles.isEmpty()) {
                return Templates.command.getrole.empty.format();
            }
            StringBuilder ret = new StringBuilder("You can request the following roles:" + "\n" + "\n");
            for (OGuildRoleAssignable role : roles) {
                ret.append("`").append(role.roleName).append("`").append("\n");
                if (!role.description.isEmpty()) {
                    ret.append(" -> ").append(role.description).append("\n");
                }
                ret.append("\n");
            }
            return ret.toString();
        }
        int startIndex = 0;
        boolean isAdding = true;
        if (args[0].equals("remove")) {
            isAdding = false;
            startIndex = 1;
        }
        if (startIndex >= args.length) {
            return Templates.invalid_use.format();
        }
        String roleName = Misc.joinStrings(args, startIndex);
        Role role = DisUtil.findRole(guild, roleName);
        if (role == null) {
            return Templates.command.getrole.not_assignable.format();
        }
        OGuildRoleAssignable roleAssignable = CGuildRoleAssignable.findBy(CGuild.getCachedId(guild.getId()), role.getId());
        if (roleAssignable.guildId == 0) {
            return Templates.command.getrole.not_assignable.format();
        }
        if (isAdding) {
            bot.out.addRole(author, role);
            if (guild.getMember(author).getRoles().contains(role)) {
                return Templates.command.getrole.not_assigned.format(role);
            }
            return Templates.command.getrole.assigned.format(role);
        }
        if (!guild.getMember(author).getRoles().contains(role)) {
            return Templates.command.getrole.not_removed.format();
        }
        bot.out.removeRole(author, role);
        return Templates.command.getrole.removed.format(role);
    }
}