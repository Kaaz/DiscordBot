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
import discordbot.core.ExitCode;
import discordbot.guildsettings.bot.SettingBotUpdateWarning;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.main.ProgramVersion;
import discordbot.util.DisUtil;
import discordbot.util.UpdateUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.TimeUnit;

/**
 * Checks if there is an update for the bot and restarts if there is
 */
public class BotSelfUpdateService extends AbstractService {

    private boolean usersHaveBeenWarned = false;

    public BotSelfUpdateService(BotContainer b) {
        super(b);
    }

    @Override
    public String getIdentifier() {
        return "bot_self_update";
    }

    @Override
    public long getDelayBetweenRuns() {
        return TimeUnit.MINUTES.toMillis(2);
    }

    @Override
    public boolean shouldIRun() {
        return Config.BOT_AUTO_UPDATE && !usersHaveBeenWarned;
    }

    @Override
    public void beforeRun() {
    }

    @Override
    public void run() {
        boolean isUpdating = false;
        ProgramVersion latestVersion = UpdateUtil.getLatestVersion();
        if (latestVersion.isHigherThan(Launcher.getVersion()) || bot.isTerminationRequested()) {
            bot.schedule(() -> {
                if (latestVersion.isHigherThan(Launcher.getVersion())) {
                    Launcher.stop(ExitCode.UPDATE);
                } else if (bot.isTerminationRequested()) {
                    Launcher.stop(bot.getRebootReason());
                } else {
                    Launcher.stop(ExitCode.NEED_MORE_SHARDS);
                }
            }, 1L, TimeUnit.MINUTES);
            usersHaveBeenWarned = true;
            String message = Template.get("announce_reboot");
            if (latestVersion.isHigherThan(Launcher.getVersion())) {
                message = Template.get("bot_self_update_restart", Launcher.getVersion().toString(), latestVersion.toString());
                isUpdating = true;
            } else if (bot.isTerminationRequested()) {
                switch (bot.getRebootReason()) {
                    case NEED_MORE_SHARDS:
                        message = Template.get("bot_reboot_more_shards");
                        break;
                    default:
                        message = Template.get("announce_reboot");
                }
            }
            for (TextChannel channel : getSubscribedChannels()) {
                sendTo(channel, message);
            }
            for (DiscordBot discordBot : this.bot.getShards()) {
                for (Guild guild : discordBot.client.getGuilds()) {
                    String announce = GuildSettings.get(guild).getOrDefault(SettingBotUpdateWarning.class);
                    switch (announce.toLowerCase()) {
                        case "off":
                            continue;
                        case "playing":
                            if (!guild.getAudioManager().isConnected()) {
                                break;
                            }
                        case "always":
                            String extraContent = "";
                            TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
                            if (defaultChannel == null || !defaultChannel.canTalk()) {
                                break;
                            }
                            if (isUpdating) {
                                extraContent += Config.EOL + Config.EOL + "You can view the changes with `" + DisUtil.getCommandPrefix(defaultChannel) + "changelog`";
                            }
                            discordBot.out.sendAsyncMessage(defaultChannel, message + extraContent, null);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

    }

    @Override
    public void afterRun() {
    }
}