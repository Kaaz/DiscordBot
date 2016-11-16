package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingLoggingChannel extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "bot_logging_channel";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"The channel where the logging of events happens. Such as users joining/leaving ",
				"",
				"Setting this to 'false' will disable it (without the quotes)",
				"",
				"To enable it, set this setting to match the channel name where you want the logging to happen",
				"If you specify an invalid channel, this setting will disable itself"
		};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && input.length() > 1;
	}
}
