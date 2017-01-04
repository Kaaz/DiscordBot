package discordbot.command.administrative.modactions;

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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;


abstract public class AbstractModActionCommand extends AbstractCommand {
	public AbstractModActionCommand() {
		super();
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"kick <user>            //kicks user",
				"kick <user> <reason..> //kicks user with a reason"
		};
	}

	protected abstract OModerationCase.PunishType getPunishType();

	protected abstract Permission getRequiredPermission();

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	protected abstract boolean punish(Guild guild, Member member);

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);
		TextChannel chan = (TextChannel) channel;
		Guild guild = chan.getGuild();
		if (getRequiredPermission() != null) {
			if (!PermissionUtil.checkPermission(guild, guild.getMember(author), getRequiredPermission())) {
				return Template.get("command_no_permission");
			}
			if (!PermissionUtil.checkPermission(guild, guild.getSelfMember(), getRequiredPermission())) {
				return Template.get("permission_missing", getRequiredPermission().name());
			}
		}
		if (args.length == 0) {
			return Template.get("command_modaction_empty", getPunishType().getKeyword());
		}
		User targetUser = DisUtil.findUser(chan, Joiner.on(" ").join(args));
		if (targetUser == null) {
			return Template.get("cant_find_user", Joiner.on(" ").join(args));
		}
		if (targetUser.getId().equals(guild.getSelfMember().getUser().getId())) {
			return Template.get("command_modaction_not_self", getPunishType().getKeyword());
		}
		if (!PermissionUtil.canInteract(guild.getSelfMember(), guild.getMember(targetUser))) {
			return Template.get("command_modaction_failed", getPunishType().getKeyword(), targetUser.getName());
		}
		int caseId = CModerationCase.insert(guild, targetUser, author, getPunishType(), null);
		TextChannel modlogChannel = bot.getModlogChannel(guild.getId());
		if (modlogChannel != null) {
			modlogChannel.sendMessage(CModerationCase.buildCase(guild, caseId)).queue(
					message -> {
						OModerationCase modcase = CModerationCase.findById(caseId);
						modcase.messageId = Long.parseLong(message.getId());
						CModerationCase.update(modcase);
					}
			);
		}
//		punish(guild, guild.getMember(targetUser));
		return Template.get("command_modaction_success", getPunishType().getKeyword(), targetUser.getName());
	}
}