package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;
import discordbot.main.Config;


public class SettingBotShowTemplates extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "bot_debug_templates";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "debug", "template"};
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String getDefault() {
		return Config.SHOW_KEYPHRASE ? "true" : "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Show which templates are being used on places.",
				"",
				"valid values: ",
				"true       -> Shows the keyphrases being used ",
				"false      -> Shows normal text ",
				"",
				"for instance if you don't have permission to access a command:",
				"",
				"setting this to true would show:",
				"no_permission",
				"",
				"false would show:",
				"You don't have permission to use that!",
		};
	}
}
