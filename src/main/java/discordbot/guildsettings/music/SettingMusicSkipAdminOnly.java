package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicSkipAdminOnly extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_skip_admin_only";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Only allow admins to use the skip command?",
				"",
				"true",
				"Only admins have permission to use the skip command",
				"",
				"false",
				"Everyone can use the skip command",
		};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
