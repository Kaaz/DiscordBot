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

package emily.command.music;

import emily.command.CommandVisibility;
import emily.command.meta.AbstractCommand;
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.handler.MusicPlayerHandler;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !volume [vol]
 * sets the volume of the music player
 * With no params returns the current volume
 */
public class VolumeCommand extends AbstractCommand {
    public VolumeCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "gets and sets the volume of the music";
    }

    @Override
    public String getCommand() {
        return "volume";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "volume              //shows current volume",
                "volume <1 to 100>   //sets volume"};
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "vol"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        if (args.length > 0) {
            if (GuildSettings.getFor(channel, GSetting.MUSIC_VOLUME_ADMIN).equals("true") && !bot.security.getSimpleRank(author, channel).isAtLeast(SimpleRank.GUILD_ADMIN)) {
                return Templates.no_permission.formatGuild(channel);
            }
            int volume;
            try {
                volume = Integer.parseInt(args[0]);
                if (volume > 0 && volume <= 100) {
                    player.setVolume(volume);
                    GuildSettings.get(guild).set(guild, GSetting.MUSIC_VOLUME, String.valueOf(player.getVolume()));
                    return Templates.command.volume_changed.formatGuild(channel, player.getVolume());
                }
            } catch (NumberFormatException ignored) {
            }
            return Templates.command.volume_invalid_parameters.formatGuild(channel);
        }
        return "Current volume: " + player.getVolume() + "%";
    }
}
