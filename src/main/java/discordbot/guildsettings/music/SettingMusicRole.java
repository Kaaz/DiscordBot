package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.RoleSettingType;


public class SettingMusicRole extends AbstractGuildSetting<RoleSettingType> {
	@Override
	protected RoleSettingType getSettingsType() {
		return new RoleSettingType(true);
	}

	@Override
	public String getKey() {
		return "music_role_requirement";
	}

	@Override
	public String[] getTags() {
		return new String[]{"music", "role", "requirement"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"In order to use music commands you need this role!",
				"Setting this value to false will disable the requirement"};
	}
}