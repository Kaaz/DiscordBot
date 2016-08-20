package novaz.handler.guildsettings.defaults;

import novaz.handler.guildsettings.AbstractGuildSetting;


public class SettingEnableChatBot extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "chat_bot_enabled";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Chat with people"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}