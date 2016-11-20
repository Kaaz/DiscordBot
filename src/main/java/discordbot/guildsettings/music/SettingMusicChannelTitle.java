package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicChannelTitle extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "music_channel_title";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Updates the music channel's topic with the currently playing song",
				"auto  -> update the title every 10 seconds with the track its playing",
				"true  -> yes change the topic at the beginning of every song",
				"false -> leave the channel topic title alone!",
		};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("auto") || input.equals("true") || input.equals("false"));
	}
}