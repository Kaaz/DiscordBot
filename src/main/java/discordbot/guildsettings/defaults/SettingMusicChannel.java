package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicChannel extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_channel";
	}

	@Override
	public String getDefault() {
		return "music";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Channel where the bots music-related output goes to"};
	}

	@Override
	public boolean isValidValue(String input) {
		return true;
	}
}