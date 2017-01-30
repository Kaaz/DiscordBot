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
import discordbot.db.controllers.CPlaylist;
import discordbot.db.model.OPlaylist;
import discordbot.guildsettings.bot.SettingMusicAdminVolume;
import discordbot.guildsettings.music.SettingMusicAutoVoiceChannel;
import discordbot.guildsettings.music.SettingMusicChannel;
import discordbot.guildsettings.music.SettingMusicClearAdminOnly;
import discordbot.guildsettings.music.SettingMusicLastPlaylist;
import discordbot.guildsettings.music.SettingMusicPlayingMessage;
import discordbot.guildsettings.music.SettingMusicQueueOnly;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.guildsettings.music.SettingMusicSkipAdminOnly;
import discordbot.guildsettings.music.SettingMusicVolume;
import discordbot.guildsettings.music.SettingMusicVotePercent;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

/**
 * !music [vol]
 */
public class MusicCommand extends AbstractCommand {
    public MusicCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "gets and sets the music-related settings";
    }

    @Override
    public String getCommand() {
        return "music";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "music                   //shows music configuration",
//				"music <pause/unpause>   //pause or resume music",
//				"music                   //shows music configuration",
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        Guild guild = ((TextChannel) channel).getGuild();
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        GuildSettings settings = GuildSettings.get(guild);

        TextChannel outputChannel = null;
        List<TextChannel> channels = guild.getTextChannelsByName(settings.getOrDefault(SettingMusicChannel.class), true);
        if (!channels.isEmpty()) {
            outputChannel = channels.get(0);
        }
        VoiceChannel autoVoice = null;
        List<VoiceChannel> vchannels = guild.getVoiceChannelsByName(settings.getOrDefault(SettingMusicAutoVoiceChannel.class), true);
        if (!vchannels.isEmpty()) {
            autoVoice = vchannels.get(0);
        }
        Role requiredRole = null;
        String roleReq = settings.getOrDefault(SettingMusicRole.class);
        if (!(roleReq.equalsIgnoreCase("none") || roleReq.equals("false"))) {
            List<Role> roles = guild.getRolesByName(roleReq, true);
            if (!roles.isEmpty()) {
                requiredRole = roles.get(0);
            }
        }
        if (args.length > 0 && rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {

            return Template.get("not_implemented_yet");
        }

        OPlaylist playlist = CPlaylist.findById(Integer.parseInt(settings.getOrDefault(SettingMusicLastPlaylist.class)));
        if (playlist.id == 0) {
            playlist = CPlaylist.getGlobalList();
        }
        String ret = "Current settings for music: " + Config.EOL + Config.EOL;
        ret += "**Required role to use music-commands:** " + Config.EOL;
        ret += (requiredRole != null ? requiredRole.getName() : "none") + Config.EOL + Config.EOL;
        ret += "**Music output text-channel:** " + Config.EOL;
        ret += (outputChannel != null ? outputChannel.getAsMention() : Emojibet.WARNING + " channel not found") + Config.EOL + Config.EOL;
        ret += "**auto-join voice-channel:** " + Config.EOL;
        ret += (autoVoice != null ? autoVoice.getName() : "disabled") + Config.EOL + Config.EOL;
        ret += "**music from queue only?**" + Config.EOL;
        ret += (settings.getOrDefault(SettingMusicQueueOnly.class).equals("true") ? "Only music from the queue will be played" : "A track from the configured playlist will be played once the queue is empty.") + Config.EOL + Config.EOL;
        ret += "**vote-skipping percentage required?**" + Config.EOL;
        ret += settings.getOrDefault(SettingMusicVotePercent.class) + "%" + Config.EOL + Config.EOL;
        ret += "**now-playing message?**" + Config.EOL;
        ret += settings.getOrDefault(SettingMusicPlayingMessage.class) + Config.EOL + Config.EOL;
        ret += "**Playlist?**" + Config.EOL;
        ret += playlist.title + Config.EOL + Config.EOL;
        ret += "**Volume:** " + Config.EOL;
        ret += settings.getOrDefault(SettingMusicVolume.class) + "%" + Config.EOL + Config.EOL;
        ret += "" + Config.EOL;
        ret += "__Admin-only options__" + Config.EOL;
        ret += "**skip the playing track?** " + Config.EOL;
        ret += (settings.getOrDefault(SettingMusicSkipAdminOnly.class).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can") + Config.EOL + Config.EOL;
        ret += "**Clear the music-queue?**" + Config.EOL;
        ret += (settings.getOrDefault(SettingMusicClearAdminOnly.class).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can") + Config.EOL + Config.EOL;
        ret += "**Change the volume?**" + Config.EOL;
        ret += (settings.getOrDefault(SettingMusicAdminVolume.class).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can") + Config.EOL + Config.EOL;
        ret += "" + Config.EOL;
        ret += "" + Config.EOL;
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Music configuration");
        embedBuilder.setDescription("These are the current settings for " + guild.getName());
        embedBuilder.addField("Required role to use music-commands", (requiredRole != null ? requiredRole.getName() : "none"), true);
        embedBuilder.addField("Music output text-channel", (outputChannel != null ? outputChannel.getAsMention() : Emojibet.WARNING + " channel not found"), true);
        embedBuilder.addField("auto-join voice-channel", (autoVoice != null ? autoVoice.getName() : "disabled"), true);
        embedBuilder.addField("music from queue only", (settings.getOrDefault(SettingMusicQueueOnly.class).equals("true") ? "Only music from the queue will be played" : "A track from the configured playlist will be played once the queue is empty."), true);
        embedBuilder.addField("vote-skipping percentage required", settings.getOrDefault(SettingMusicVotePercent.class) + "%", true);
        embedBuilder.addField("now-playing message", settings.getOrDefault(SettingMusicPlayingMessage.class), true);
        embedBuilder.addField("Playlist", playlist.title, true);
        embedBuilder.addField("Volume", settings.getOrDefault(SettingMusicVolume.class) + "%", true);
        embedBuilder.addBlankField(true);
        embedBuilder.addBlankField(false);
        embedBuilder.addField("skip the playing track", (settings.getOrDefault(SettingMusicSkipAdminOnly.class).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can"), true);
        embedBuilder.addField("Clear the music-queue", (settings.getOrDefault(SettingMusicClearAdminOnly.class).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can"), true);
        embedBuilder.addField("Change the volume", (settings.getOrDefault(SettingMusicAdminVolume.class).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can"), true);
        if (PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
            channel.sendMessage(embedBuilder.build()).queue();
            return "";
        }
        return ret;
    }
}
