package discordbot.guildsettings.moderation;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.TextChannelSettingType;


public class SettingCommandLoggingChannel extends AbstractGuildSetting<TextChannelSettingType> {
	@Override
	protected TextChannelSettingType getSettingsType() {
		return new TextChannelSettingType(true);
	}

	@Override
	public String getKey() {
		return "bot_command_logging_channel";
	}

	@Override
	public String[] getTags() {
		return new String[]{"command", "logging", "channel", "mod"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"The channel command usage will be logged to",
				"",
				"Example output:",
				"Kaaz#9436 has used `say` in #general",
				"aruments: this is not a test",
				"output: this is not a test",
				"",
				"Setting this to 'false' will disable it (without the quotes)",
				"",
				"To enable it, set this setting to match the channel name where you want the command logging to happen",
				"If you specify an invalid channel, this setting will disable itself"
		};
	}
}
