package discordbot.guildsettings.bot;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.StringLengthSettingType;
import discordbot.main.Config;


public class SettingCommandPrefix extends AbstractGuildSetting<StringLengthSettingType> {
	@Override
	protected StringLengthSettingType getSettingsType() {
		return new StringLengthSettingType(1, 4);
	}

	@Override
	public String getKey() {
		return "command_prefix";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "prefix", "command"};
	}

	@Override
	public String getDefault() {
		return Config.BOT_COMMAND_PREFIX;
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Prefix for commands (between 1 and 4 characters)"};
	}
}
