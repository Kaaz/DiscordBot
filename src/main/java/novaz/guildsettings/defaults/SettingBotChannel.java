package novaz.guildsettings.defaults;

import novaz.guildsettings.AbstractGuildSetting;


public class SettingBotChannel extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "bot_channel";
	}

	@Override
	public String getDefault() {
		return "general";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Channel where the bots default output goes to"};
	}

	@Override
	public boolean isValidValue(String input) {
		return true;
	}
}
