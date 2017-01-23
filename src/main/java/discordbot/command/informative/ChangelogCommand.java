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

package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBotVersionChanges;
import discordbot.db.controllers.CBotVersions;
import discordbot.db.model.OBotVersion;
import discordbot.db.model.OBotVersionChange;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.main.ProgramVersion;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
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
				return Template.get("permission_missing", Permission.MESSAGE_EMBED_LINKS);
			}
			channel.sendMessage(message).queue();
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
		String desc = "";
		OBotVersionChange.ChangeType lastType = null;
		for (OBotVersionChange change : changes) {
			if (!change.changeType.equals(lastType)) {
				lastType = change.changeType;
				desc += String.format("\n**%s %s**\n", lastType.getEmoji(), lastType.getTitle().toUpperCase());
			}

			desc += String.format(" • %s\n", change.description);
		}
		b.setTitle("[" + version.toString() + "] Changelog " + (dbVersion.published == 0 ? Emojibet.WARNING + " Still being worked on!" : ""));
		b.setDescription(desc);
		b.setFooter(String.format("I'd love to hear your feedback, feel free to join %sdiscord", DisUtil.getCommandPrefix(channel)), channel.getJDA().getSelfUser().getAvatarUrl());
		return b.build();
	}
}