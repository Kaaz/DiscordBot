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

package emily.command.administrative.modactions;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.db.controllers.CModerationCase;
import emily.db.model.OModerationCase;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.DisUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;


abstract public class AbstractModActionCommand extends AbstractCommand {
    public AbstractModActionCommand() {
        super();
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                String.format("%s <user>     //%s user from guild", getCommand(), getPunishType().getDescription()),
        };
    }

    protected abstract OModerationCase.PunishType getPunishType();

    protected abstract Permission getRequiredPermission();

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    protected abstract boolean punish(DiscordBot bot, Guild guild, Member member);

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        SimpleRank rank = bot.security.getSimpleRank(author);
        TextChannel chan = (TextChannel) channel;
        Guild guild = chan.getGuild();
        if (getRequiredPermission() != null) {
            if (!PermissionUtil.checkPermission(guild, guild.getMember(author), getRequiredPermission())) {
                return Template.get("command_no_permission");
            }
            if (!PermissionUtil.checkPermission(guild, guild.getSelfMember(), getRequiredPermission())) {
                return Template.get("permission_missing", getRequiredPermission().name());
            }
        }
        if (args.length == 0) {
            return Template.get("command_modaction_empty", getPunishType().getKeyword().toLowerCase());
        }
        User targetUser = DisUtil.findUser(chan, Joiner.on(" ").join(args));
        if (targetUser == null) {
            return Template.get("cant_find_user", Joiner.on(" ").join(args));
        }
        if (targetUser.getId().equals(guild.getSelfMember().getUser().getId())) {
            return Template.get("command_modaction_not_self", getPunishType().getKeyword().toLowerCase());
        }
        if (!PermissionUtil.canInteract(guild.getSelfMember(), guild.getMember(targetUser)) || !punish(bot, guild, guild.getMember(targetUser))) {
            return Template.get("command_modaction_failed", getPunishType().getKeyword().toLowerCase(), targetUser.getName());
        }
        int caseId = CModerationCase.insert(guild, targetUser, author, getPunishType(), null);
        TextChannel modlogChannel = bot.getModlogChannel(guild.getId());
        if (modlogChannel != null) {
            bot.queue.add(modlogChannel.sendMessage(CModerationCase.buildCase(guild, caseId)),
                    message -> {
                        OModerationCase modCase = CModerationCase.findById(caseId);
                        modCase.messageId = message.getId();
                        CModerationCase.update(modCase);
                    });

        }
        return Template.get("command_modaction_success", targetUser.getName(), getPunishType().getVerb().toLowerCase());
    }
}