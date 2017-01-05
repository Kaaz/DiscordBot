package discordbot.guildsettings.bot;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;

/**
 * Made by nija123098 on 12/4/2016
 */
public class SettingMusicAdminVolume extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "music_volume_admin";
	}

	@Override
	public String[] getTags() {
		return new String[]{"music", "volume", "admin"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Require a guild admin to change the volume",
				"",
				"true -> only allow guild admins to change the bot's volume",
				"false -> allow all users to change the bot's volume"};
	}
}
