package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingPMUserEvents extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "pm_user_events";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Send a private message to owner when something happens to a user?",
				"true  -> sends a private message to guild-owner",
				"false -> does absolutely nothing"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
