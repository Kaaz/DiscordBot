package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.NoSettingType;


public class SettingMusicLastPlaylist extends AbstractGuildSetting<NoSettingType> {
	@Override
	protected NoSettingType getSettingsType() {
		return new NoSettingType();
	}

	@Override
	public String getKey() {
		return "music_playlist_id";
	}

	@Override
	public String[] getTags() {
		return new String[]{"music", "playlist"};
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
}