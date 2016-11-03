package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicRole extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_role_requirement";
	}

	@Override
	public String getDefault() {
		return "none";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"In order to use music commands you need this role!",
				"Setting this value to none will disable the requirement"};
	}

	@Override
	public boolean isValidValue(String input) {
		return true;
	}
}