package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingShowUnknownCommands extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "show_unknown_commands";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Show message on nonexistent commands",
				"true -> returns a help message",
				"false -> stays silent"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
