package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicShowListeners extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_show_listeners";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Show who's listening in the *current* command",
				"true  -> List all the people who are currently listening to music",
				"false -> Don't show listeners"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
