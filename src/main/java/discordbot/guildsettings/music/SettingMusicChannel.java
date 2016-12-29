package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.TextChannelSettingType;


public class SettingMusicChannel extends AbstractGuildSetting<TextChannelSettingType> {

	@Override
	protected TextChannelSettingType getSettingsType() {
		return new TextChannelSettingType();
	}

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
}