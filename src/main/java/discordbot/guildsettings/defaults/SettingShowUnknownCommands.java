package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingShowUnknownCommands extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "show_unknown_commands";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Show message on nonexistent commands and blacklisted commands",
				"true -> returns a help message",
				"false -> stays silent"};
	}
}
