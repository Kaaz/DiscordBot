package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingMusicSkipAdminOnly extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "music_skip_admin_only";
	}

	@Override
	public String[] getTags() {
		return new String[]{"music", "skip", "admin"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public boolean isReadOnly() {
		return true;
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
}
