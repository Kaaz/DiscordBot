package discordbot.command.administrative;

import discordbot.command.administrative.modactions.AbstractModActionCommand;
import discordbot.db.model.OModerationCase;
import discordbot.guildsettings.moderation.SettingMuteRole;
import discordbot.handler.GuildSettings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * command for muting users in a guild
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
		Role role = guild.getRoleById(GuildSettings.get(guild).getOrDefault(SettingMuteRole.class));
		if (role == null) {
			return false;
		}
		List<Role> roles = member.getRoles();
		List<Role> rolesToRemove = new ArrayList<>();
		for (Role r : roles) {
			if (r.isManaged()) {
				continue;
			}
			if (!PermissionUtil.canInteract(guild.getSelfMember(), r)) {
				continue;
			}
			rolesToRemove.add(r);
		}
		guild.getController().removeRolesFromMember(member, rolesToRemove).queue(aVoid ->
				guild.getController().addRolesToMember(member, role).queue()
		);
		return true;
	}
}