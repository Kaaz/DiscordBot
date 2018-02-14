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

package emily.command.informative;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Emojibet;
import emily.util.GfxUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerCommand extends AbstractCommand {
    public ServerCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Information about the server";
    }

    @Override
    public String getCommand() {
        return "server";
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        if (!PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
            return Templates.permission_missing.format(Permission.MESSAGE_EMBED_LINKS.toString());
        }
        if (bot.security.getSimpleRank(author, channel).isAtLeast(SimpleRank.BOT_ADMIN) && args.length > 0 && DisUtil.matchesGuildSearch(args[0])) {
            guild = DisUtil.findGuildBy(args[0], bot.getContainer());
            if (guild == null) {
                return Templates.config.cant_find_guild.format(args[0]);
            }
        }
        EmbedBuilder b = new EmbedBuilder();
        b.setAuthor(guild.getName(), guild.getIconUrl(), guild.getIconUrl());
        b.setThumbnail(guild.getIconUrl());

        b.setDescription(
                "Discord-id `" + guild.getId() + "`" + "\n" +
                        "On shard `" + bot.getShardId() + "`" + "\n" +
                        (PermissionUtil.checkPermission(guild.getSelfMember(), Permission.ADMINISTRATOR)
                                ? Emojibet.POLICE + " Administrator" : "")
        );
        ImmutableSet<OnlineStatus> onlineStatus = Sets.immutableEnumSet(OnlineStatus.ONLINE, OnlineStatus.IDLE, OnlineStatus.DO_NOT_DISTURB);
        long online = guild.getMembers().stream().filter(member -> onlineStatus.contains(member.getOnlineStatus())).count();
        b.setColor(GfxUtil.getAverageColor(guild.getIconUrl()));
        b.addField("Members", String.format("%s online\n%s in total", online, guild.getMembers().size()), true);
        b.addField("Channels", String.format("%s text channels\n%s voice channels", guild.getTextChannels().size(), guild.getVoiceChannels().size()), true);
        b.addField("Created by", String.format("%s\\#%s", guild.getOwner().getUser().getName(), guild.getOwner().getUser().getDiscriminator()), true);
        b.addField("My prefix", String.format("`%s`", DisUtil.getCommandPrefix(guild)), true);
        b.addField("Created On", new SimpleDateFormat("dd MMMM yyyy").format(new Date(guild.getCreationTime().toInstant().toEpochMilli())), true);
        b.setFooter(guild.getSelfMember().getEffectiveName(), channel.getJDA().getSelfUser().getAvatarUrl());
        bot.queue.add(channel.sendMessage(b.build()));
        return "";
    }
}