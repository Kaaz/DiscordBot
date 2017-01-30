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
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CModerationCase;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OModerationCase;
import discordbot.guildsettings.moderation.SettingModlogChannel;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Misc;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class CaseCommand extends AbstractCommand {
    public CaseCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Moderate the mod-cases";
    }

    @Override
    public String getCommand() {
        return "case";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "case reason <id> <message>  //sets/modifies the reason of a case",
                "case reason last <message> //sets/modified the reason of the last added case by you"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        Guild guild = ((TextChannel) channel).getGuild();
        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Template.get("command_no_permission");
        }
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "reason":
                    if (args.length < 3) {
                        return Template.get("command_invalid_use");
                    }
                    return editReason(guild, guild.getMember(author), channel, args[1], Misc.joinStrings(args, 2));
            }
        }
        return Template.get("command_invalid_use");
    }

    private String editReason(Guild guild, Member moderator, MessageChannel feedbackChannel, String caseId, String reason) {

        OModerationCase oCase;
        if (caseId.equalsIgnoreCase("last")) {
            oCase = CModerationCase.findLastFor(CGuild.getCachedId(guild.getId()), CUser.getCachedId(moderator.getUser().getId()));
        } else {
            oCase = CModerationCase.findById(Misc.parseInt(caseId, -1));
        }
        if (oCase.id == 0 || oCase.guildId != CGuild.getCachedId(guild.getId())) {
            return Template.get("command_case_not_found", oCase.id);
        }
        oCase.reason = reason;
        CModerationCase.update(oCase);
        TextChannel channel = guild.getTextChannelById(GuildSettings.get(guild).getOrDefault(SettingModlogChannel.class));
        if (channel == null) {
            return Template.get("guild_channel_modlog_not_found");
        }
        channel.getMessageById(oCase.messageId).queue(
                message -> {
                    message.editMessage(new MessageBuilder().setEmbed(CModerationCase.buildCase(guild, oCase)).build()).queue();
                    feedbackChannel.sendMessage(Template.get("command_case_reason_modified")).queue();
                }
                , throwable ->
                        feedbackChannel.sendMessage(Template.get("command_case_message_unknown")).queue()


        );
        return "";
    }
}