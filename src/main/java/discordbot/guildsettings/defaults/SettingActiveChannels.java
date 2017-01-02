package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.EnumSettingType;


public class SettingActiveChannels extends AbstractGuildSetting<EnumSettingType> {

	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("mine", "all");
	}

	@Override
	public String getKey() {
		return "bot_listen";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot","listen"};
	}

	@Override
	public String getDefault() {
		return "all";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"What channels to listen to? (all;mine)",
				"all -> responds to all channels",
				"mine -> only responds to messages in configured channel"};
	}
}
