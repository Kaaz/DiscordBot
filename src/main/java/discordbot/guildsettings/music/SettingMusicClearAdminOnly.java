package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicClearAdminOnly extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_clear_admin_only";
	}

	@Override
	public String getDefault() {
		return "true";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Only allow admins to clear the music queue?",
				"",
				"true",
				"Only admins can clear the music queue",
				"",
				"false",
				"Everyone can clear the queue",
		};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
