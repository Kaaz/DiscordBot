package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.TextChannelSettingType;


public class SettingBotChannel extends AbstractGuildSetting<TextChannelSettingType> {
	@Override
	protected TextChannelSettingType getSettingsType() {
		return new TextChannelSettingType(false);
	}

	@Override
	public String getKey() {
		return "bot_channel";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "channel"};
	}

	@Override
	public String getDefault() {
		return "general";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Channel where the bots default output goes to"};
	}
}
