package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingMusicShowListeners extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "music_show_listeners";
	}

	@Override
	public String[] getTags() {
		return new String[]{"music", "show", "listeners"};
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Show who's listening in the *current* command",
				"true  -> List all the people who are currently listening to music",
				"false -> Don't show listeners"
		};
	}
}
