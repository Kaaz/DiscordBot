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

import emily.command.administrative.modactions.AbstractModActionCommand;
import emily.db.model.OModerationCase;
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * command for muting users in a guild
 */
public class MuteCommand extends AbstractModActionCommand {
    @Override
    public String getDescription() {
        return "Mute a member from your guild";
    }

    @Override
    public String getCommand() {
        return "mute";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    protected OModerationCase.PunishType getPunishType() {
        return OModerationCase.PunishType.MUTE;
    }

    @Override
    protected Permission getRequiredPermission() {
        return Permission.MANAGE_ROLES;
    }

    @Override
    protected boolean punish(DiscordBot bot, Guild guild, Member member) {
        Role role = guild.getRoleById(GuildSettings.get(guild).getOrDefault(GSetting.BOT_MUTE_ROLE));
        if (role == null) {
            return false;
        }
        List<Role> roles = member.getRoles();

        List<Role> rolesToAdd = new ArrayList<>();
        rolesToAdd.add(role);
        List<Role> rolesToRemove = new ArrayList<>();
        for (Role r : roles) {
            if (r.isManaged()) {
                continue;
            }
            if (!PermissionUtil.canInteract(guild.getSelfMember(), r)) {
                continue;
            }
            if (r.equals(role)) {
                roles.remove(role);
                continue;
            }
            rolesToRemove.add(r);
        }
        bot.queue.add(guild.getController().modifyMemberRoles(member, rolesToAdd, rolesToRemove));
        return true;
    }
}