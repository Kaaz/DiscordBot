package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingHelpInPM extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "help_in_pm";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"show help in a private message?" +
				"true  -> send a message to the user requesting help",
				"false -> output help to the channel where requested"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
