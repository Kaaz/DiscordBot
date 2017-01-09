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
		MessageEmbed message = null;
		ProgramVersion version = Launcher.getVersion();
		if (args.length == 0) {
			message = printVersion(version);
		} else {
			version = ProgramVersion.fromString(args[0]);
		}
		if (message != null) {
			if (channel instanceof TextChannel && PermissionUtil.checkPermission((TextChannel) channel, ((TextChannel) channel).getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
				return Template.get("permission_missing", Permission.MESSAGE_EMBED_LINKS);
			}
			channel.sendMessage(message).queue();
			return "";
		}
		return "No changes for version " + version.toString();
	}

	private MessageEmbed printVersion(ProgramVersion version) {
		EmbedBuilder b = new EmbedBuilder();
		OBotVersion dbVersion = CBotVersions.findBy(version);
		if (dbVersion == null || dbVersion.published == 0) {
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
				desc += String.format("**%s %s**\n", lastType.getEmoji(), lastType.getTitle());
			}
			desc += String.format(" > %s\n", change.description);
		}
		b.setTitle("Changelog for [" + version.toString() + "]");
		b.setDescription(desc);
		return b.build();
	}
}