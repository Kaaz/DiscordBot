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

package emily.event;

import emily.db.controllers.CBotEvent;
import emily.db.controllers.CGuild;
import emily.db.controllers.CGuildMember;
import emily.db.controllers.CUser;
import emily.db.model.OGuild;
import emily.db.model.OGuildMember;
import emily.db.model.OUser;
import emily.guildsettings.GSetting;
import emily.handler.GuildSettings;
import emily.handler.MusicPlayerHandler;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.main.GuildCheckResult;
import emily.main.Launcher;
import emily.role.RoleRankings;
import emily.templates.Template;
import emily.templates.Templates;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * Created on 12-10-2016
 */
public class JDAEvents extends ListenerAdapter {
    private final DiscordBot discordBot;

    public JDAEvents(DiscordBot bot) {
        this.discordBot = bot;
    }

    public void onDisconnect(DisconnectEvent event) {
        DiscordBot.LOGGER.info("[event] DISCONNECTED! ");
    }

    @Override
    public void onStatusChange(StatusChangeEvent event) {
        discordBot.getContainer().reportStatus(event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardId() : 0, event.getOldStatus(), event.getStatus());
    }

    @Override
    public void onResume(ResumedEvent event) {
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        discordBot.getContainer().reportError(String.format("[RECONCT] \\#%02d with a different JDA", discordBot.getShardId()));
    }

    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        User owner = guild.getOwner().getUser();
        OUser user = CUser.findBy(owner.getId());
        user.discord_id = owner.getId();
        user.name = EmojiUtils.shortCodify(owner.getName());
        CUser.update(user);
        OGuild dbGuild = CGuild.findBy(guild.getId());
        dbGuild.discord_id = Long.parseLong(guild.getId());
        dbGuild.name = EmojiUtils.shortCodify(guild.getName());
        dbGuild.owner = user.id;
        if (dbGuild.id == 0) {
            CGuild.insert(dbGuild);
        }
        if (dbGuild.isBanned()) {
            discordBot.queue.add(guild.leave());
            return;
        }
        discordBot.loadGuild(guild);
        String cmdPre = GuildSettings.get(guild).getOrDefault(GSetting.COMMAND_PREFIX);
        GuildCheckResult guildCheck = discordBot.security.checkGuild(guild);
        if (dbGuild.active != 1) {
            String message = "Thanks for adding me to your guild!" + BotConfig.EOL +
                    "To see what I can do you can type the command `" + cmdPre + "help`." + BotConfig.EOL +
                    "Most of my features are opt-in, which means that you'll have to enable them first. Admins can use `" + cmdPre + "config` to change my settings." + BotConfig.EOL +
                    "Most commands has a help portion which can be accessed by typing help after the command; For instance: `" + cmdPre + "skip help` " + BotConfig.EOL + BotConfig.EOL +
                    "If you need help or would like to give feedback, feel free to let me know on either `" + cmdPre + "discord` or `" + cmdPre + "github`";
            switch (guildCheck) {
                case TEST_GUILD:
                    message += BotConfig.EOL + BotConfig.EOL + " :warning: The guild has been categorized as a test guild. This means that I might leave this guild when the next cleanup happens." + BotConfig.EOL +
                            "If this is not a test guild feel free to join my `" + cmdPre + "discord` and ask to have your guild added to the whitelist!";
                    break;
                case BOT_GUILD:
                    message += BotConfig.EOL + BotConfig.EOL + ":warning: :robot: Too many bots here, I'm leaving! " + BotConfig.EOL +
                            "If your guild is not a collection of bots and you actually plan on using me join my `" + cmdPre + "discord` and ask to have your guild added to the whitelist!";
                    break;
                case SMALL:
                case OWNER_TOO_NEW:
                case OKE:
                default:
                    break;
            }
            TextChannel outChannel = null;
            for (TextChannel channel : guild.getTextChannels()) {
                if (channel.canTalk()) {
                    outChannel = channel;
                    break;
                }
            }
            CBotEvent.insert(":house:", ":white_check_mark:",
                    String.format(":id: %s | :hash: %s | :busts_in_silhouette: %s | %s",
                            guild.getId(),
                            dbGuild.id,
                            guild.getMembers().size(),
                            EmojiUtils.shortCodify(guild.getName())).replace("@", "@\u200B"));
            discordBot.getContainer().guildJoined();
            Launcher.log("bot joins guild", "bot", "guild-join",
                    "guild-id", guild.getId(),
                    "guild-name", guild.getName());
            if (outChannel != null) {
                discordBot.out.sendAsyncMessage(outChannel, message, null);
            } else {
                discordBot.out.sendPrivateMessage(owner, message);
            }
            if (guildCheck.equals(GuildCheckResult.BOT_GUILD)) {
                discordBot.queue.add(guild.leave());
            }
            dbGuild.active = 1;
        }
        CGuild.update(dbGuild);
        DiscordBot.LOGGER.info("[event] JOINED SERVER! " + guild.getName());
        discordBot.sendStatsToDiscordPw();
        discordBot.getContainer().sendStatsToDiscordlistNet();
        for (Member member : event.getGuild().getMembers()) {
            User guildUser = member.getUser();
            int userId = CUser.getCachedId(guildUser.getId(), guildUser.getName());
            OGuildMember guildMember = CGuildMember.findBy(dbGuild.id, userId);
            guildMember.joinDate = new Timestamp(member.getJoinDate().toInstant().toEpochMilli());
            CGuildMember.insertOrUpdate(guildMember);
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        OGuild server = CGuild.findBy(guild.getId());
        server.active = 0;
        CGuild.update(server);
        discordBot.clearGuildData(guild);
        discordBot.getContainer().guildLeft();
        if (server.isBanned()) {
            return;
        }
        discordBot.sendStatsToDiscordPw();
        discordBot.getContainer().sendStatsToDiscordlistNet();
        Launcher.log("bot leaves guild", "bot", "guild-leave",
                "guild-id", guild.getId(),
                "guild-name", guild.getName());
        CBotEvent.insert(":house_abandoned:", ":fire:",
                String.format(":id: %s | :hash: %s | %s",
                        guild.getId(),
                        server.id,
                        EmojiUtils.shortCodify(guild.getName()).replace("@", "@\u200B")
                ));
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        handleReaction(event, true);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        handleReaction(event, false);
    }

    private void handleReaction(GenericMessageReactionEvent e, boolean adding) {
        if (e.getUser().isBot()) {
            if (!discordBot.security.isInteractionBot(Long.parseLong(e.getUser().getId()))) {
                return;
            }
        }
        if (!e.getChannel().getType().equals(ChannelType.TEXT)) {
            return;
        }
        TextChannel channel = (TextChannel) e.getChannel();
        if (discordBot.commandReactionHandler.canHandle(channel.getGuild().getId(), e.getMessageId())) {
            discordBot.commandReactionHandler.handle(channel, e.getMessageId(), e.getUser().getId(), e.getReaction());
            return;
        }
        if (!discordBot.gameHandler.executeReaction(e.getUser(), e.getChannel(), e.getReaction(), e.getMessageId())) {
            if (!discordBot.musicReactionHandler.handle(e.getMessageId(), channel, e.getUser(), e.getReactionEmote(), adding)) {
                discordBot.roleReactionHandler.handle(e.getMessageId(), channel, e.getUser(), e.getReactionEmote(), adding);
            }
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        discordBot.handleMessage(event.getGuild(), event.getChannel(), event.getAuthor(), event.getMessage());
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        discordBot.handlePrivateMessage(event.getChannel(), event.getAuthor(), event.getMessage());
    }


    @Override
    public void onGuildBan(GuildBanEvent event) {
        discordBot.logGuildEvent(event.getGuild(), "\uD83D\uDED1", "**" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + "** has been banned");
    }

    @Override
    public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
        String message = "**" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator() + "** changed nickname ";
        if (event.getPrevNick() != null) {
            message += "from _~~" + event.getPrevNick() + "~~_ ";
        }
        if (event.getNewNick() != null) {
            message += "to **" + event.getNewNick() + "**";
        } else {
            message += "back to normal";
        }
        discordBot.logGuildEvent(event.getGuild(), "\uD83C\uDFF7", message);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getMember().getUser();
        Guild guild = event.getGuild();
        GuildSettings settings = GuildSettings.get(guild);
        OGuildMember guildMember = CGuildMember.findBy(guild.getId(), user.getId());
        boolean firstTime = guildMember.joinDate == null;
        guildMember.joinDate = new Timestamp(System.currentTimeMillis());
        CGuildMember.insertOrUpdate(guildMember);

        if ("true".equals(settings.getOrDefault(GSetting.PM_USER_EVENTS))) {
            discordBot.out.sendPrivateMessage(guild.getOwner().getUser(), String.format("[user-event] **%s#%s** joined the guild **%s**", user.getName(), user.getDiscriminator(), guild.getName()),
                    null
            );
        }
        discordBot.logGuildEvent(guild, "\uD83D\uDC64", "**" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator() + "** joined the guild");
        if ("true".equals(settings.getOrDefault(GSetting.WELCOME_NEW_USERS))) {
            TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
            if (defaultChannel != null && defaultChannel.canTalk() && !discordBot.security.isBotAdmin(user.getIdLong())) {
                Template template = firstTime ? Templates.welcome_new_user : Templates.welcome_back_user;
                discordBot.queue.add(defaultChannel.sendMessage(
                        template.formatGuild(guild.getId(), guild, user)),
                        message -> {
                            if (!"no".equals(settings.getOrDefault(GSetting.CLEANUP_MESSAGES))) {
                                discordBot.schedule(() -> discordBot.out.saveDelete(message), BotConfig.DELETE_MESSAGES_AFTER * 5, TimeUnit.MILLISECONDS);
                            }
                        });
            } else if (defaultChannel != null && defaultChannel.canTalk() && discordBot.security.isBotAdmin(user.getIdLong())) {
                Template template = Templates.welcome_bot_admin;
                discordBot.queue.add(defaultChannel.sendMessage(template.formatGuild(guild.getId(), guild, user)),
                        message -> {
                            if (!"no".equals(settings.getOrDefault(GSetting.CLEANUP_MESSAGES))) {
                                discordBot.schedule(() -> discordBot.out.saveDelete(message), BotConfig.DELETE_MESSAGES_AFTER * 5, TimeUnit.MILLISECONDS);
                            }
                        });

            }
        }
        Launcher.log("user joins guild", "guild", "member-join",
                "guild-id", guild.getId(),
                "guild-name", guild.getName(),
                "user-id", user.getId(),
                "user-name", user.getName());

        if ("true".equals(settings.getOrDefault(GSetting.USER_TIME_RANKS)) && !user.isBot()) {
            RoleRankings.assignUserRole(discordBot, guild, user);
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        User user = event.getMember().getUser();
        if (user.isBot()) {
            return;
        }
        Guild guild = event.getGuild();
        if ("true".equals(GuildSettings.get(guild).getOrDefault(GSetting.PM_USER_EVENTS))) {
            discordBot.out.sendPrivateMessage(guild.getOwner().getUser(), String.format("[user-event] **%s#%s** left the guild **%s**", user.getName(), user.getDiscriminator(), guild.getName()));
        }
        if ("true".equals(GuildSettings.get(guild).getOrDefault(GSetting.WELCOME_NEW_USERS))) {
            TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
            if (defaultChannel != null && defaultChannel.canTalk()) {
                discordBot.queue.add(defaultChannel.sendMessage(
                        Templates.message_user_leaves.formatGuild(guild.getId(), user, guild)),
                        message -> {
                            if (!"no".equals(GuildSettings.get(guild.getId()).getOrDefault(GSetting.CLEANUP_MESSAGES))) {
                                discordBot.schedule(() -> discordBot.out.saveDelete(message), BotConfig.DELETE_MESSAGES_AFTER * 5, TimeUnit.MILLISECONDS);
                            }
                        });
            }
        }
        Launcher.log("user leaves guild", "guild", "member-leave",
                "guild-id", guild.getId(),
                "guild-name", guild.getName(),
                "user-id", user.getId(),
                "user-name", user.getName());
        OGuildMember guildMember = CGuildMember.findBy(guild.getId(), user.getId());
        guildMember.joinDate = new Timestamp(System.currentTimeMillis());
        CGuildMember.insertOrUpdate(guildMember);
        discordBot.logGuildEvent(guild, "\uD83C\uDFC3", "**" + user.getName() + "#" + user.getDiscriminator() + "** left the guild");
    }

    @Override
    public void onUserGameUpdate(UserGameUpdateEvent event) {
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        MusicPlayerHandler player = MusicPlayerHandler.getFor(event.getGuild(), discordBot);
        if (player.isConnected()) {
            return;
        }
        String autoChannel = GuildSettings.get(event.getGuild()).getOrDefault(GSetting.MUSIC_CHANNEL_AUTO);
        if ("false".equalsIgnoreCase(autoChannel)) {
            return;
        }
        if (event.getChannelJoined().getId().equals(autoChannel) || event.getChannelJoined().getName().equalsIgnoreCase(autoChannel)) {
            player.connectTo(event.getChannelJoined());
            player.playRandomSong();
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (!event.getMember().equals(event.getGuild().getSelfMember())) {
            checkLeaving(event.getGuild(), event.getChannelLeft(), event.getMember().getUser());
            onGuildVoiceJoin(new GuildVoiceJoinEvent(event.getJDA(), 0, event.getMember()));
        } else {
            checkLeaving(event.getGuild(), event.getChannelJoined(), event.getMember().getUser());
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        checkLeaving(event.getChannelLeft().getGuild(), event.getChannelLeft(), event.getMember().getUser());
    }

    private void checkLeaving(Guild guild, VoiceChannel channel, User user) {
        if (user.isBot() && !user.equals(user.getJDA().getSelfUser())) {
            return;
        }
        MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, discordBot);
        if (!player.isConnected()) {
            return;
        }
        if (!player.isConnectedTo(channel)) {
            return;
        }
        player.unregisterVoteSkip(user);
        if (player.getVoteCount() >= player.getRequiredVotes()) {
            player.forceSkip();
        }
        for (Member member : guild.getAudioManager().getConnectedChannel().getMembers()) {
            if (!member.getUser().isBot()) {
                return;
            }
        }
        player.leave();
        String autoChannel = GuildSettings.get(guild).getOrDefault(GSetting.MUSIC_CHANNEL_AUTO);
        if (!"false".equalsIgnoreCase(autoChannel) && channel.getName().equalsIgnoreCase(autoChannel)) {
            return;
        }
        TextChannel musicChannel = discordBot.getMusicChannel(guild);
        if (musicChannel != null && musicChannel.canTalk()) {
            discordBot.out.sendAsyncMessage(musicChannel, Templates.music.no_one_listens_i_leave.formatGuild(guild.getId()));
        }
    }
}