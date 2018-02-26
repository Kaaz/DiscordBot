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
 * !skip
 * skips current active track
 */
public class SkipTrack extends AbstractCommand {
    public SkipTrack() {
        super();
    }

    @Override
    public String getDescription() {
        return "skip current track";
    }

    @Override
    public String getCommand() {
        return "skip";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "skip                  //skips current track",
                "skip adminonly        //check what skipmode its set on",
                "skip adminonly toggle //toggle the skipmode",
                "skip force            //admin-only, force a skip"
//				"skip perm //skips permanently; never hear this song again"
        };
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "next"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
        SimpleRank userRank = bot.security.getSimpleRank(author, channel);
        boolean adminOnly = "true".equals(GuildSettings.getFor(channel, GSetting.MUSIC_SKIP_ADMIN_ONLY));
        if (adminOnly && !userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            return Templates.music.skip_admin_only.formatGuild(channel);
        }
        if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
            return Templates.music.required_role_not_found.formatGuild(channel, guild.getRoleById(GuildSettings.getFor(channel, GSetting.MUSIC_ROLE_REQUIREMENT)));
        }
        if (!player.isPlaying()) {
            return Templates.command.currentlyplaying.nosong.formatGuild(channel);
        }
        if (!player.isInVoiceWith(guild, author)) {
            return Templates.music.not_same_voicechannel.formatGuild(channel);
        }
        if (args.length >= 1) {
            switch (args[0]) {
                case "force":
                    if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
                        player.forceSkip();
                        return "";
                    }
                    return Templates.music.skip_admin_only.formatGuild(channel);
                case "perm":
                case "permanent":
                    return Templates.command.skip_permanent_success.formatGuild(channel);
                case "admin":
                case "adminonly":
                    if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN) && args.length > 1 && args[1].equalsIgnoreCase("toggle")) {
                        GuildSettings.get(guild).set(guild, GSetting.MUSIC_SKIP_ADMIN_ONLY, adminOnly ? "false" : "true");
                        adminOnly = !adminOnly;
                    }
                    return Templates.music.skip_mode.formatGuild(channel, adminOnly ? "admin-only" : "normal");
                default:
                    return Templates.invalid_use.formatGuild(channel);
            }
        }
        if (player.getRequiredVotes() == 1) {
            player.forceSkip();
            return "";
        }
        boolean voteRegistered = player.voteSkip(author);
        if (player.getVoteCount() >= player.getRequiredVotes()) {
            player.forceSkip();
            return Templates.command.skip_song_skipped.formatGuild(channel);
        }
        if (voteRegistered) {
            return Templates.command.skip_vote_success.formatGuild(channel, player.getVoteCount(), player.getRequiredVotes());
        }
        return Templates.command.skip_vote_failed.formatGuild(channel);
    }
}
