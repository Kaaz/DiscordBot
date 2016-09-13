package novaz.guildsettings.defaults;

import novaz.guildsettings.AbstractGuildSetting;


public class SettingGameModule extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "module_games";
	}

	@Override
	public String getDefault() {
		return "true";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Let people play games against each other"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}