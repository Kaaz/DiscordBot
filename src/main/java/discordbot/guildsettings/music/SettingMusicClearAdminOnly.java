package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingMusicClearAdminOnly extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "music_clear_admin_only";
	}

	@Override
	public String[] initTags() {
		return new String[]{"music", "admin", "clear"};
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
}
