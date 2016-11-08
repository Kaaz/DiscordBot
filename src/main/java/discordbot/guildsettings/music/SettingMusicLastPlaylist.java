package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicLastPlaylist extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_playlist_id";
	}

	@Override
	public String getDefault() {
		return "0";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"used to store the last used playlist "
		};
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public boolean isValidValue(String input) {
		return true;
	}
}