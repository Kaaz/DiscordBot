package discordbot.guildsettings.moderation;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.RoleSettingType;


public class SettingMuteRole extends AbstractGuildSetting<RoleSettingType> {
	@Override
	protected RoleSettingType getSettingsType() {
		return new RoleSettingType(true);
	}

	@Override
	public String getKey() {
		return "bot_mute_role";
	}

	@Override
	public String[] getTags() {
		return new String[]{"mod", "role", "punish"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"This is the role which is applied to those who you use the mute command on",
				"",
				"Setting this value to false will disable the role applied with the mute command"};
	}
}