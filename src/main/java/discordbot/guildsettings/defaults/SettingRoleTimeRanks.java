package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingRoleTimeRanks extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "user_time_ranks";
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

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
