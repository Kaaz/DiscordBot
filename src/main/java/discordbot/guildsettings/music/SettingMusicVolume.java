package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.NumberBetweenSettingType;


public class SettingMusicVolume extends AbstractGuildSetting<NumberBetweenSettingType> {
	@Override
	protected NumberBetweenSettingType getSettingsType() {
		return new NumberBetweenSettingType(0, 100);
	}

	@Override
	public String getKey() {
		return "music_volume";
	}

	@Override
	public String getDefault() {
		return "10";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"sets the default volume of the music player",
				"So the next time the bot connects it starts with this volume",
				"",
				"Accepts a value between 0 and 100"
		};
	}
}