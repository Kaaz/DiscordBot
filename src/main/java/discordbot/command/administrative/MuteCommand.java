package discordbot.command.administrative;

import discordbot.command.administrative.modactions.AbstractModActionCommand;
import discordbot.db.model.OModerationCase;
import discordbot.guildsettings.moderation.SettingMuteRole;
import discordbot.handler.GuildSettings;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

/**
 * command for kicking users from a guild
 */
public class MuteCommand extends AbstractModActionCommand {
	@Override
	public String getDescription() {
		return "Mute a member from your guild";
	}

	@Override
	public String getCommand() {
		return "mute";
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	protected OModerationCase.PunishType getPunishType() {
		return OModerationCase.PunishType.MUTE;
	}

	@Override
	protected Permission getRequiredPermission() {
		return Permission.MANAGE_ROLES;
	}

	@Override
	protected boolean punish(Guild guild, Member member) {
		Role role = DisUtil.findRole(guild, GuildSettings.get(guild).getOrDefault(SettingMuteRole.class));
		if (role == null) {
			return false;
		}
		guild.getController().addRolesToMember(member, role).queue();
		return true;
	}
}