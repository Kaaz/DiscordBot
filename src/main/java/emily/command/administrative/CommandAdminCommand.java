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

import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.db.controllers.CBlacklistCommand;
import emily.db.controllers.CGuild;
import emily.db.model.OBlacklistCommand;
import emily.handler.CommandHandler;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Emojibet;
import net.dv8tion.jda.core.entities.*;

import java.util.List;

/**
 * !disable/enable commands per guild/channel
 */
public class CommandAdminCommand extends AbstractCommand {
    public CommandAdminCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Commands can be enabled/disabled through this command." + "\n" +
                "A channel specific setting will always override the guild setting" + "\n" + "\n" +
                "You can also give/deny permission to roles to use certain commands";
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String getCommand() {
        return "commandadmin";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "ca <command> [enable/disable]               //enables/disables commands in the whole guild",
                "ca <command> [enable/disable] [#channel]    //enables/disables commands in a channel. This overrides the above",
                "ca all-commands [enable/disable]            //disable/enable all (disable-able commands)",
                "ca all-commands [enable/disable] [#channel] //disable/enable all commands in that channel",
                "",
                "ca resetchannel [#channel]                  //resets the overrides for a channel",
                "ca resetallchannels                         //resets the overrides for all channels",
                "ca reset yesimsure                          //enables all commands + resets overrides",
//				"",
//				"//Allow roles to use certain commands, or not. The channel tag is optional!",
//				"ca role allow <role> <command> [#channel]   //Allows a role to use a command it otherwise couldn't ",
//				"ca role deny <role> <command> [#channel]    //Denies the use of a command it otherwise could ",
//				"ca role remove <role> <command> [#channel]  //Removes the override command from the role",
//				"ca role [#channel]                          //Overview of role overrides",
//				"ca role reset @role                         //Resets a role",
                "",
                "examples:",
                "ca meme disable                             //this disabled the meme command",
                "ca meme enable #spam                        //overrides and meme is enabled in #spam"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "ca"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        TextChannel textChannel = (TextChannel) channel;
        Guild guild = textChannel.getGuild();
        if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Templates.no_permission.formatGuild(channel);
        }
        int guildId = CGuild.getCachedId(channel);
        if (args.length == 0) {
            List<OBlacklistCommand> blacklist = CBlacklistCommand.getBlacklistedFor(guildId);
            if (blacklist.isEmpty()) {
                return Templates.command.blacklist.command_empty.formatGuild(channel);
            }
            StringBuilder ret = new StringBuilder().append("The following commands are restricted: ").append("\n").append("\n");
            String lastCommand = blacklist.get(0).command;
            boolean guildwide = false;
            boolean firstSubItem = true;
            for (OBlacklistCommand item : blacklist) {
                String icon = item.blacklisted ? Emojibet.NO_ENTRY : Emojibet.OKE_SIGN;
                String cmdStatus = item.blacklisted ? "disabled" : "enabled";
                if (!lastCommand.equals(item.command)) {
                    lastCommand = item.command;
                    ret.append("\n").append("\n");
                    guildwide = false;
                    firstSubItem = true;
                }
                if (item.channelId.equals("0")) {
                    ret.append(icon).append(" `").append(item.command).append("` is ").append(cmdStatus).append(" guild-wide!").append("\n");
                    guildwide = true;
                } else {
                    TextChannel tmp = channel.getJDA().getTextChannelById(item.channelId);
                    if (tmp == null) {
                        continue;
                    }
                    if (!guildwide && firstSubItem) {
                        ret.append("`").append(item.command).append("` is ").append(cmdStatus).append(" in: ");
                    }
                    if (!firstSubItem) {
                        ret.append(" | ");
                    }
                    if (firstSubItem && guildwide) {
                        ret.append("Except in: ");
                    }
                    firstSubItem = false;
                    ret.append(tmp.getAsMention()).append(" ").append(icon);
                }
            }
            return ret.toString();
        }
        switch (args[0].toLowerCase()) {
            case "resetchannel":
                if (args.length != 2) {
                    return Templates.invalid_use.formatGuild(channel);
                }
                String channelId = DisUtil.mentionToId(args[1]);
                TextChannel c = channel.getJDA().getTextChannelById(channelId);
                if (c == null) {
                    return Templates.invalid_use.formatGuild(channel);
                }
                CBlacklistCommand.deleteOverridesInChannel(guildId, channelId);
                CommandHandler.reloadBlackListFor(guildId);
                return Templates.command.blacklist.reset_channel.formatGuild(channel, c.getAsMention());
            case "resetallchannels":
                CBlacklistCommand.deleteAllOverrides(guildId);
                CommandHandler.reloadBlackListFor(guildId);
                return Templates.command.blacklist.reset_all_channels.formatGuild(channel);
            case "reset":
                if (args.length != 2) {
                    return Templates.invalid_use.formatGuild(channel);
                }
                CBlacklistCommand.deleteGuild(guildId);
                CommandHandler.reloadBlackListFor(guildId);
                return Templates.command.blacklist.reset.formatGuild(channel);
        }
        if (args[0].equals("role")) {
            if (args.length < 2) {
                Templates.not_implemented_yet.formatGuild(channel);
            }
            if (args.length < 4) {
                Templates.invalid_use.formatGuild(channel);
            }
            String type = args[1];
            String roleName = args[2];
            String commandName = args[3];
            //ca role allow @role command
            Role role = DisUtil.findRole(guild, roleName);
            AbstractCommand cmd = CommandHandler.getCommand(commandName.toLowerCase());
            if (cmd == null) {
                return Templates.command.blacklist.command_not_found.formatGuild(channel, commandName);
            }
            if (!cmd.canBeDisabled()) {
                return Templates.command.blacklist.not_blacklistable.formatGuild(channel, cmd.getCommand());
            }

            if (role == null) {
                return "role not found";
            }
            return "Action = " + type;
        }
        if (args.length < 2) {
            return Templates.invalid_use.formatGuild(channel);
        }
        AbstractCommand command = CommandHandler.getCommand(args[0].toLowerCase());
        String commandName;
        if (args[0].equals("all-commands")) {
            commandName = args[0];
        } else {
            if (command == null) {
                return Templates.command.blacklist.command_not_found.formatGuild(channel, args[0]);
            }
            if (!command.canBeDisabled()) {
                return Templates.command.blacklist.not_blacklistable.formatGuild(channel, args[0]);
            }
            commandName = command.getCommand();
        }
        if (!args[1].equals("enable") && !args[1].equals("disable")) {
            return Templates.invalid_use.formatGuild(channel);
        }
        boolean blacklist = args[1].equals("disable");
        String channelId = "0";//guild-wide
        if (args.length > 2) {
            if (!DisUtil.isChannelMention(args[2])) {
                return Templates.invalid_use.formatGuild(channel);
            }
            channelId = DisUtil.mentionToId(args[2]);
            TextChannel c = channel.getJDA().getTextChannelById(channelId);
            if (c == null) {
                return Templates.invalid_use.formatGuild(channel);
            }
        }
        if (blacklist) {
            CBlacklistCommand.insertOrUpdate(guildId, commandName, channelId, true);
            CommandHandler.reloadBlackListFor(guildId);
            return Templates.command.blacklist.command_disabled.formatGuild(channel, commandName);
        } else {
            if (!channelId.equals("0")) {
                CBlacklistCommand.insertOrUpdate(guildId, commandName, channelId, false);
            } else {
                CBlacklistCommand.delete(guildId, commandName, channelId);
            }
            CommandHandler.reloadBlackListFor(guildId);
            return Templates.command.blacklist.command_enabled.formatGuild(channel, commandName);
        }
    }
}