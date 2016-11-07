package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicVolume extends AbstractGuildSetting {
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

	@Override
	public boolean isValidValue(String input) {
		try {
			int vol = Integer.parseInt(input);
			if (vol >= 0 && vol <= 100) {
				return true;
			}
		} catch (Exception ignored) {
		}
		return false;
	}
}