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

package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.guildsettings.bot.SettingRoleTimeRanks;
import discordbot.handler.GuildSettings;
import discordbot.main.BotContainer;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.role.RoleRankings;
import net.dv8tion.jda.core.entities.Guild;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * updates the ranking of members within a guild
 */
public class UserRankingSystemService extends AbstractService {

    public UserRankingSystemService(BotContainer b) {
        super(b);
    }

    @Override
    public String getIdentifier() {
        return "user_role_ranking";
    }

    @Override
    public long getDelayBetweenRuns() {
        return TimeUnit.MINUTES.toMillis(15);
    }

    @Override
    public boolean shouldIRun() {
        return true;
    }

    @Override
    public void beforeRun() {
    }

    @Override
    public void run() {
        for (DiscordBot discordBot : bot.getShards()) {
            List<Guild> guilds = discordBot.client.getGuilds();
            for (Guild guild : guilds) {
                GuildSettings settings = GuildSettings.get(guild);
                if (settings != null && "true".equals(settings.getOrDefault(SettingRoleTimeRanks.class)) && RoleRankings.canModifyRoles(guild, discordBot.client.getSelfUser())) {
                    try {
                        handleGuild(discordBot, guild);
                    } catch (Exception e) {
                        Launcher.logToDiscord(e, "guild", guild.getId(), "name", guild.getName());
                    }
                }
            }
        }
    }

    private void handleGuild(DiscordBot bot, Guild guild) {
        RoleRankings.fixForServer(guild);
        guild.getMembers().stream().filter(user -> !user.getUser().isBot()).forEach(user -> RoleRankings.assignUserRole(bot, guild, user.getUser()));
    }

    @Override
    public void afterRun() {
        System.gc();
    }
}