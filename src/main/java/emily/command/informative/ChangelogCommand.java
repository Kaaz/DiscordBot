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

import emily.command.meta.AbstractCommand;
import emily.db.controllers.CBotVersionChanges;
import emily.db.controllers.CBotVersions;
import emily.db.model.OBotVersion;
import emily.db.model.OBotVersionChange;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.main.ProgramVersion;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Emojibet;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

public class ChangelogCommand extends AbstractCommand {
    public ChangelogCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Check out whats new";
    }

    @Override
    public String getCommand() {
        return "changelog";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "changelog               //shows changes for the latest version",
                "changelog next          //shows changes for the latest version",
                "changelog <version>     //shows changes for that version",
                "",
                "example:",
                "changelog 1.9.6",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MessageEmbed message;
        ProgramVersion version;
        if (args.length == 0) {
            version = Launcher.getVersion();
        } else if (args[0].equalsIgnoreCase("next")) {
            version = CBotVersions.versionAfter(Launcher.getVersion()).getVersion();
        } else {
            version = ProgramVersion.fromString(args[0]);
        }
        message = printVersion(channel, version, bot.security.getSimpleRank(author, channel));
        if (message != null) {
            if (channel instanceof TextChannel && !PermissionUtil.checkPermission((TextChannel) channel, ((TextChannel) channel).getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
                return Templates.permission_missing.formatGuild(channel, Permission.MESSAGE_EMBED_LINKS.toString());
            }
            bot.queue.add(channel.sendMessage(message));
            return "";
        }
        return "No changes for version " + version.toString();
    }

    private MessageEmbed printVersion(MessageChannel channel, ProgramVersion version, SimpleRank rank) {
        EmbedBuilder b = new EmbedBuilder();
        OBotVersion dbVersion = CBotVersions.findBy(version);
        if (!rank.isAtLeast(SimpleRank.BOT_ADMIN) && dbVersion.published == 0) {
            return null;
        }
        List<OBotVersionChange> changes = CBotVersionChanges.getChangesFor(dbVersion.id);
        if (changes.isEmpty()) {
            return null;
        }
        StringBuilder desc = new StringBuilder();
        OBotVersionChange.ChangeType lastType = null;
        for (OBotVersionChange change : changes) {
            if (!change.changeType.equals(lastType)) {
                lastType = change.changeType;
                desc.append(String.format("\n**%s %s**\n", lastType.getEmoji(), lastType.getTitle().toUpperCase()));
            }

            desc.append(String.format(" • %s\n", change.description));
        }
        b.setTitle("[" + version.toString() + "] Changelog " + (dbVersion.published == 0 ? Emojibet.WARNING + " Still being worked on!" : ""), null);
        b.setDescription(desc.toString());
        b.setFooter(String.format("I'd love to hear your feedback, feel free to join %sdiscord", DisUtil.getCommandPrefix(channel)), channel.getJDA().getSelfUser().getAvatarUrl());
        return b.build();
    }
}