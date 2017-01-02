package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingWelcomeNewUsers extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "welcome_new_users";
	}

	@Override
	public String[] getTags() {
		return new String[]{"message", "welcome", "user"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Show a welcome message to new users?",
				"Valid options:",
				"true  -> shows a welcome when a user joins or leaves the guild",
				"false -> Disabled, doesn't say anything",
				"",
				"The welcome message can be set with the template: ",
				"welcome_new_user",
				"",
				"The welcome back message can be set with the template (if the user had joined before): ",
				"welcome_back_user",
				"",
				"The leave message can be set with the template: ",
				"message_user_leaves",
				"",
				"If multiple templates are set a random one will be chosen",
				"See the template command for more details"};
	}
}
