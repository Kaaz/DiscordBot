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

import emily.command.meta.CommandVisibility;
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
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * !pause
 * pause the music or resume it
 */
public class PauseCommand extends AbstractCommand {
    public PauseCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "pauses the music or resumes it if its paused";
    }

    @Override
    public String getCommand() {
        return "pause";
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "resume"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
            return Templates.music.required_role_not_found.formatGuild(channel, guild.getRoleById(GuildSettings.getFor(channel, GSetting.MUSIC_ROLE_REQUIREMENT)));
        }
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        if (!player.canTogglePause()) {
            return Templates.music.state_not_started.formatGuild(channel);
        }
        VoiceChannel userVoice = guild.getMember(author).getVoiceState().getChannel();
        if (userVoice == null || !player.isConnectedTo(userVoice)) {
            return Templates.music.not_same_voicechannel.formatGuild(channel);
        }
        if (player.togglePause()) {
            return Templates.music.state_paused.formatGuild(channel);
        }
        return Templates.music.state_resumed.formatGuild(channel);
    }
}