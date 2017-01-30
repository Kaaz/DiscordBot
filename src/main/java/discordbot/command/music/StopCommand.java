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

package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !stop
 * make the bot stop playing music
 */
public class StopCommand extends AbstractCommand {
    public StopCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "stops playing music";
    }

    @Override
    public String getCommand() {
        return "stop";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "stop          //stops playing and leaves the channel",
                "stop force    //stops playing and leaves the channel (admin, debug)",
                "stop afternp  //stops and leaves after the now playing track is over",
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "leave"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        Guild guild = ((TextChannel) channel).getGuild();
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
            return Template.get(channel, "music_required_role_not_found", GuildSettings.getFor(channel, SettingMusicRole.class));
        }
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        if (args.length > 0) {
            if (args[0].equals("force") && userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
                player.leave();
                return Template.get("command_stop_success");
            }
        }
        if (!player.isPlaying()) {
            return Template.get("command_currentlyplaying_nosong");
        }
        if (player.isConnected()) {
            if (!player.canUseVoiceCommands(author, userRank)) {
                return Template.get("music_not_same_voicechannel");
            }
            if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN) && player.aListenerIsAtLeast(SimpleRank.GUILD_ADMIN)) {
                return Template.get("music_not_while_admin_listening");
            }
            if (args.length > 0 && args[0].equals("afternp")) {
                player.stopAfterTrack(true);
                return Template.get("command_stop_after_track");
            } else {
                player.leave();
            }
            return Template.get("command_stop_success");
        }
        return Template.get("command_currentlyplaying_nosong");

    }
}