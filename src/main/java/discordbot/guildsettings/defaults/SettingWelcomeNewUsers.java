package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingWelcomeNewUsers extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "welcome_new_users";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Show a welcome message to new users?",
				"true  -> shows a welcome message to new users",
				"false -> stays silent"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
