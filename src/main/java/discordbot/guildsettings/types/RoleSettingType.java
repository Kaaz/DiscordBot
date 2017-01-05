package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

/**
 * TextChannel settings type
 * the value has to be a real channel in a guild + will be saved as the channel id
 */
public class RoleSettingType implements IGuildSettingType {

	private final boolean allowNull;

	/**
	 * Allow a null/false value?
	 *
	 * @param allowNull true if it can be null
	 */
	public RoleSettingType(boolean allowNull) {

		this.allowNull = allowNull;
	}

	@Override
	public String typeName() {
		return "discord-role";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		if (allowNull && (value == null || value.isEmpty() || value.equalsIgnoreCase("false"))) {
			return true;
		}
		if (DisUtil.isRoleMention(value)) {
			return guild.getRoleById(DisUtil.mentionToId(value)) != null;
		}
		return DisUtil.findRole(guild, value) != null;
	}

	@Override
	public String fromInput(Guild guild, String value) {
		if (allowNull && (value == null || value.isEmpty() || value.equalsIgnoreCase("false"))) {
			return "false";
		}
		if (DisUtil.isRoleMention(value)) {
			Role role = guild.getRoleById(DisUtil.mentionToId(value));
			if (role != null) {
				return role.getId();
			}
		}
		Role role = DisUtil.findRole(guild, value);
		if (role != null) {
			return role.getId();
		}
		return "false";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		Role role = guild.getRoleById(value);
		if (role != null) {
			return role.getName();
		}
		if (!value.isEmpty() && !value.matches("\\d{10,}")) {
			Role roleByName = DisUtil.findRole(guild, value);
			if (roleByName != null) {
				return roleByName.getName();
			}
		}
		return Emojibet.X;
	}
}
