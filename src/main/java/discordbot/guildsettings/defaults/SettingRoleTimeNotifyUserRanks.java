package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.EnumSettingType;


public class SettingRoleTimeNotifyUserRanks extends AbstractGuildSetting<EnumSettingType> {
	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("no", "false", "private", "public", "both");
	}

	@Override
	public String getKey() {
		return "user_time_ranks_notify";
	}

	@Override
	public String[] initTags() {
		return new String[]{"user", "rank", "warn"};
	}

	@Override
	public String getDefault() {
		return "no";
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Send a notification whenever a user goes up a rank?",
				"no      -> Don't notify anyone, stay silent!",
				"false   -> Don't notify anyone, stay silent!",
				"private -> send a private message to the user who ranked up",
				"public  -> announce it in a channel",
				"both    -> perform both private and public actions "};
	}
}
