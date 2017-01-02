package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingAutoReplyModule extends AbstractGuildSetting<BooleanSettingType> {

	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "auto_reply";
	}

	@Override
	public String[] initTags() {
		return new String[]{"bot","reply","auto"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"use the auto reply feature?",
				"Looks for patterns in messages and replies to them (with a cooldown)",
				"true -> enable auto replying to matched messages",
				"false -> disable auto replying",
		};
	}
}