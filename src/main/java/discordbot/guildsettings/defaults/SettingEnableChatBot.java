package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;
import discordbot.main.Config;


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
	public String[] initTags() {
		return new String[]{"bot", "chat", "module"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Chat with people" + Config.EOL +
				"" + Config.EOL +
				"Setting this to true will make it so that it responds to every message in the configured bot_channel"
		};
	}
}