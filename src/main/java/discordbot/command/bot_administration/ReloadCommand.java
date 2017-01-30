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

package discordbot.command.bot_administration;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !reload
 * reloads config
 */
public class ReloadCommand extends AbstractCommand {
    public ReloadCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "reloads the configuration";
    }

    @Override
    public String getCommand() {
        return "reload";
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        Guild guild = ((TextChannel) channel).getGuild();
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        if (rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
            bot.loadConfiguration();
            return Template.get("command_reload_success");
        }
        if (rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            bot.clearChannels(guild);
            bot.loadGuild(guild);
            return Template.get("command_reload_success");
        }
        return Template.get("no_permission");
    }
}