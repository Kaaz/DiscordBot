package novaz.guildsettings.defaults;

import novaz.guildsettings.AbstractGuildSetting;


public class SettingRoleTimeRanksPrefix extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "user_time_ranks_prefix";
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

	@Override
	public boolean isValidValue(String input) {
		return input != null && input.length() >= 3 && input.length() <= 8;
	}
}
