package discordbot.command.administrative;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CModerationCase;
import discordbot.db.model.OModerationCase;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

/**
 * command for kicking users from a guild
 */
public class KickCommand extends AbstractCommand {
	public KickCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "kicks a user";
	}

	@Override
	public String getCommand() {
		return "kick";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"kick <user>            //kicks user",
				"kick <user> <reason..> //kicks user with a reason"
		};
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);
		TextChannel chan = (TextChannel) channel;
		Guild guild = chan.getGuild();
		if (!PermissionUtil.checkPermission(guild, guild.getMember(author), Permission.KICK_MEMBERS)) {
			return Template.get("command_no_permission");
		}
		if (!PermissionUtil.checkPermission(guild, guild.getSelfMember(), Permission.KICK_MEMBERS)) {
			return Template.get("permission_missing_kick_members");
		}
		if (args.length == 0) {
			return Template.get("command_kick_empty");
		}
		User targetUser = DisUtil.findUser(chan, Joiner.on(" ").join(args));
		if (targetUser == null) {
			return Template.get("cant_find_user", Joiner.on(" ").join(args));
		}
		if (targetUser.getId().equals(guild.getSelfMember().getUser().getId())) {
			return Template.get("command_kick_not_self");
		}
		if (!PermissionUtil.canInteract(guild.getSelfMember(), guild.getMember(targetUser))) {
			return Template.get("command_kick_failed", targetUser.getName());
		}
		int caseId = CModerationCase.insert(guild, targetUser, author, OModerationCase.PunishType.KICK, null);
		chan.sendMessage(CModerationCase.buildCase(guild, caseId)).queue();
		return "Not finished yet!";
//		guild.getController().kick(guild.getMember(author)).queue();
//		return Emojibet.OKE_SIGN;
	}
}