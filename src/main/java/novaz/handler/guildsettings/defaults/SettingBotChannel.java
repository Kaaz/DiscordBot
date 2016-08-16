package novaz.handler.guildsettings.defaults;

import novaz.handler.guildsettings.AbstractGuildSetting;


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
	public String getDescription() {
		return "Channel where the bots default output goes to";
	}
}
