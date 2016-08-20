package novaz.handler.guildsettings.defaults;

import novaz.handler.guildsettings.AbstractGuildSetting;


public class SettingCommandPrefix extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "command_prefix";
	}

	@Override
	public String getDefault() {
		return "!";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Prefix for commands (between 1 and 3 characters)"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && input.length() > 0 && input.length() <= 3;
	}
}
