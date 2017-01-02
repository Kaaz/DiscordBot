package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.EnumSettingType;


public class SettingMusicChannelTitle extends AbstractGuildSetting<EnumSettingType> {
	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("auto", "true", "false");
	}

	@Override
	public String getKey() {
		return "music_channel_title";
	}

	@Override
	public String[] initTags() {
		return new String[]{"music", "channel", "title"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Updates the music channel's topic with the currently playing song",
				"",
				"auto  -> update the title every 10 seconds with the track its playing",
				"true  -> yes change the topic at the beginning of every song",
				"false -> leave the channel topic title alone!",
		};
	}
}