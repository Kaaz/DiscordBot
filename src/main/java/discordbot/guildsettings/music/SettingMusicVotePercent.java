package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.NumberBetweenSettingType;


public class SettingMusicVotePercent extends AbstractGuildSetting<NumberBetweenSettingType> {
	@Override
	protected NumberBetweenSettingType getSettingsType() {
		return new NumberBetweenSettingType(0, 100);
	}

	@Override
	public String getKey() {
		return "music_vote_percent";
	}

	@Override
	public String getDefault() {
		return "40";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Percentage of users (rounded down) required to skip the currently playing track",
				"",
				"eg; when set to 25, and 5 listeners it would require 2 users to vote skip ",
				"",
				"Accepts a value between 1 and 100",
		};
	}
}