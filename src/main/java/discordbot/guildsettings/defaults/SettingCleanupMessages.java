package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.EnumSettingType;


public class SettingCleanupMessages extends AbstractGuildSetting<EnumSettingType> {

	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("yes", "no", "nonstandard");
	}

	@Override
	public String getKey() {
		return "cleanup_messages";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "cleanup", "messages"};
	}

	@Override
	public String getDefault() {
		return "no";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Delete messages after a while?",
				"yes         -> Always delete messages",
				"no          -> Never delete messages",
				"nonstandard -> delete messages outside of bot's default channel"};
	}
}
