package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingAutoReplyModule extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "auto_reply";
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
				"true -> disable auto replying",
		};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}