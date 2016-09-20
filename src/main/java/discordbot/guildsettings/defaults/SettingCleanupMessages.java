package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingCleanupMessages extends AbstractGuildSetting {
	@Override
	public String getKey() {
		return "cleanup_messages";
	}

	@Override
	public String getDefault() {
		return "no";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Delete messages after a while? (yes;no;nonstandard)",
				"yes -> Always delete messages",
				"no -> Never delete messages",
				"nonstandard -> delete messages outside of bot's default channel"};
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("yes") || input.equals("no") || input.equals("nonstandard"));
	}
}
