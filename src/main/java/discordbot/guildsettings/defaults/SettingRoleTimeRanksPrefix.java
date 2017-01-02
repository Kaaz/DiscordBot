package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.StringLengthSettingType;


public class SettingRoleTimeRanksPrefix extends AbstractGuildSetting<StringLengthSettingType> {
	@Override
	protected StringLengthSettingType getSettingsType() {
		return new StringLengthSettingType(3, 8);
	}

	@Override
	public String getKey() {
		return "user_time_ranks_prefix";
	}

	@Override
	public String[] initTags() {
		return new String[]{"user", "rank", "prefix"};
	}

	@Override
	public String getDefault() {
		return "[rank]";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"The prefix of the role name for the time based role ranking",
				"Using this prefix to manage roles so make sure its somewhat unique! Or you'll have to cleanup yourself :)",
				"If you'd like to use the time based ranks make sure to set this first!",
				"",
				"The prefix can be between 3 and 8 in length"};
	}
}
