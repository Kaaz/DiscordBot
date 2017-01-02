package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingPMUserEvents extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "pm_user_events";
	}

	@Override
	public String[] initTags() {
		return new String[]{"user", "admin", "events"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Send a private message to owner when something happens to a user?",
				"true  -> sends a private message to guild-owner",
				"false -> does absolutely nothing"};
	}
}
