package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.TextChannelSettingType;


public class SettingModlogChannel extends AbstractGuildSetting<TextChannelSettingType> {
	@Override
	protected TextChannelSettingType getSettingsType() {
		return new TextChannelSettingType(true);
	}

	@Override
	public String getKey() {
		return "bot_modlog_channel";
	}

	@Override
	public String[] getTags() {
		return new String[]{"case", "logging", "channel"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"The channel where mod-logging happens.",
				"A case will appear if a user has been banned/kicked/warned/muted",
				"",
				"Setting this to 'false' will disable it (without the quotes)",
				"",
				"To enable it, set this setting to match the channel name where you want the moderation-cases to go",
				"If you specify an invalid channel, this setting will disable itself"
		};
	}
}
