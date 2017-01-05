package discordbot.guildsettings.bot;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingGameModule extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "module_games";
	}

	@Override
	public String[] getTags() {
		return new String[]{"bot", "module", "games"};
	}

	@Override
	public String getDefault() {
		return "true";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Let people play games against each other"};
	}
}