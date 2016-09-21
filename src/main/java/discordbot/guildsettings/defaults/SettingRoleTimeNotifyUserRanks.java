package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingRoleTimeNotifyUserRanks extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "user_time_ranks_notify";
	}

	@Override
	public String getDefault() {
		return "no";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Send a notification whenever a user goes up a rank?",
				"no      -> Don't notify anyone, stay silent!",
				"private -> send a private message to the user who ranked up",
				"public  -> announce it in a channel",
				"both    -> perform both private and public actions "};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("no") || input.equals("private") || input.equals("public") || input.equals("both"));
	}
}
