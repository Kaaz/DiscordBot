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
import emily.core.AbstractCommand;
import emily.db.controllers.CPlaylist;
import emily.db.model.OPlaylist;
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.handler.MusicPlayerHandler;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.Emojibet;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        SimpleRank rank = bot.security.getSimpleRank(author, channel);
        GuildSettings settings = GuildSettings.get(guild);

        TextChannel outputChannel = null;
        List<TextChannel> channels = guild.getTextChannelsByName(settings.getOrDefault(GSetting.MUSIC_CHANNEL), true);
        if (!channels.isEmpty()) {
            outputChannel = channels.get(0);
        }
        VoiceChannel autoVoice = null;
        List<VoiceChannel> vchannels = guild.getVoiceChannelsByName(settings.getOrDefault(GSetting.MUSIC_CHANNEL_AUTO), true);
        if (!vchannels.isEmpty()) {
            autoVoice = vchannels.get(0);
        }
        Role requiredRole = null;
        String roleReq = settings.getOrDefault(GSetting.MUSIC_ROLE_REQUIREMENT);
        if (!(roleReq.equalsIgnoreCase("none") || roleReq.equals("false"))) {
            List<Role> roles = guild.getRolesByName(roleReq, true);
            if (!roles.isEmpty()) {
                requiredRole = roles.get(0);
            }
        }
        if (args.length > 0 && rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {

            return Templates.not_implemented_yet.formatGuild(channel);
        }

        OPlaylist playlist = CPlaylist.findById(Integer.parseInt(settings.getOrDefault(GSetting.MUSIC_PLAYLIST_ID)));
        if (playlist.id == 0) {
            playlist = CPlaylist.getGlobalList();
        }
        String ret = "Current settings for music: " + "\n" + "\n";
        ret += "**Required role to use music-commands:** " + "\n";
        ret += (requiredRole != null ? requiredRole.getName() : "none") + "\n" + "\n";
        ret += "**Music output text-channel:** " + "\n";
        ret += (outputChannel != null ? outputChannel.getAsMention() : Emojibet.WARNING + " channel not found") + "\n" + "\n";
        ret += "**auto-join voice-channel:** " + "\n";
        ret += (autoVoice != null ? autoVoice.getName() : "disabled") + "\n" + "\n";
        ret += "**music from queue only?**" + "\n";
        ret += (settings.getOrDefault(GSetting.MUSIC_QUEUE_ONLY).equals("true") ? "Only music from the queue will be played" : "A track from the configured playlist will be played once the queue is empty.") + "\n" + "\n";
        ret += "**vote-skipping percentage required?**" + "\n";
        ret += settings.getOrDefault(GSetting.MUSIC_VOTE_PERCENT) + "%" + "\n" + "\n";
        ret += "**now-playing message?**" + "\n";
        ret += settings.getOrDefault(GSetting.MUSIC_PLAYING_MESSAGE) + "\n" + "\n";
        ret += "**Playlist?**" + "\n";
        ret += playlist.title + "\n" + "\n";
        ret += "**Volume:** " + "\n";
        ret += settings.getOrDefault(GSetting.MUSIC_VOLUME) + "%" + "\n" + "\n";
        ret += "" + "\n";
        ret += "__Admin-only options__" + "\n";
        ret += "**skip the playing track?** " + "\n";
        ret += (settings.getOrDefault(GSetting.MUSIC_SKIP_ADMIN_ONLY).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can") + "\n" + "\n";
        ret += "**Clear the music-queue?**" + "\n";
        ret += (settings.getOrDefault(GSetting.MUSIC_CLEAR_ADMIN_ONLY).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can") + "\n" + "\n";
        ret += "**Change the volume?**" + "\n";
        ret += (settings.getOrDefault(GSetting.MUSIC_VOLUME_ADMIN).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can") + "\n" + "\n";
        ret += "" + "\n";
        ret += "" + "\n";
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Music configuration", null);
        embedBuilder.setDescription("These are the current settings for " + guild.getName() + "\n ** use the Config command to change these, this is an overview!**");
        embedBuilder.addField("Required role to use music-commands", (requiredRole != null ? requiredRole.getName() : "none"), true);
        embedBuilder.addField("Music output text-channel", (outputChannel != null ? outputChannel.getAsMention() : Emojibet.WARNING + " channel not found"), true);
        embedBuilder.addField("auto-join voice-channel", (autoVoice != null ? autoVoice.getName() : "disabled"), true);
        embedBuilder.addField("music from queue only", (settings.getOrDefault(GSetting.MUSIC_QUEUE_ONLY).equals("true") ? "Only music from the queue will be played" : "A track from the configured playlist will be played once the queue is empty."), true);
        embedBuilder.addField("vote-skipping percentage required", settings.getOrDefault(GSetting.MUSIC_VOTE_PERCENT) + "%", true);
        embedBuilder.addField("now-playing message", settings.getOrDefault(GSetting.MUSIC_PLAYING_MESSAGE), true);
        embedBuilder.addField("Playlist", playlist.title, true);
        embedBuilder.addField("Volume", settings.getOrDefault(GSetting.MUSIC_VOLUME) + "%", true);
        embedBuilder.addBlankField(true);
        embedBuilder.addBlankField(false);
        embedBuilder.addField("skip the playing track", (settings.getOrDefault(GSetting.MUSIC_SKIP_ADMIN_ONLY).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can"), true);
        embedBuilder.addField("Clear the music-queue", (settings.getOrDefault(GSetting.MUSIC_CLEAR_ADMIN_ONLY).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can"), true);
        embedBuilder.addField("Change the volume", (settings.getOrDefault(GSetting.MUSIC_VOLUME_ADMIN).equals("true") ? Emojibet.NO_ENTRY + " Only admins" : Emojibet.OKE_SIGN + " Anyone can"), true);
        if (PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
            bot.queue.add(channel.sendMessage(embedBuilder.build()));
            return "";
        }
        return ret;
    }
}
