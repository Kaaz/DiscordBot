package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingRoleTimeRanks extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "user_time_ranks";
	}

	@Override
	public String[] initTags() {
		return new String[]{"user", "rank", "auto"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"This setting will require me to have the manage role permission!",
				"Users are given a role based on their time spend in the discord server",
				"If you'd like to use the time based ranks, be sure to check out the other settings first!",
				"Setting:  Use time based ranks?",
				"true  -> yes",
				"false -> no"};
	}
}
