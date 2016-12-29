package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingBotUpdateWarning extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "bot_update_warning";
	}

	@Override
	public String getDefault() {
		return "playing";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Show a warning that there is an update and that the bot will be updating soon.",
				"always  -> always show the message in the bot's configured default channel",
				"playing -> only announce when the bot is playing music and in the bot's configured music channel",
				"off     -> don't announce when the bot is going down for an update"
		};
	}
}
