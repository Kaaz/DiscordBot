package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.main.Config;


public class SettingCommandPrefix extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "command_prefix";
	}

	@Override
	public String getDefault() {
		return Config.BOT_COMMAND_PREFIX;
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
