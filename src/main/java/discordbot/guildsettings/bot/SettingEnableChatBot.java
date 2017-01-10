package discordbot.guildsettings.bot;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingEnableChatBot extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "chat_bot_enabled";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "chat", "module"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Chat with people",
				"",
				"Setting this to true will make it so that it responds to every message in the configured bot_channel"
		};
	}
}