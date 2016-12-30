package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.BooleanSettingType;


public class SettingMusicQueueOnly extends AbstractGuildSetting<BooleanSettingType> {
	@Override
	protected BooleanSettingType getSettingsType() {
		return new BooleanSettingType();
	}

	@Override
	public String getKey() {
		return "music_queue_only";
	}

	@Override
	public String getDefault() {
		return "false";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Stop playing music once the queue is empty?",
				"",
				"true",
				"once the queue is empty I stop playing music and leave the voice channel",
				"",
				"false",
				"If the queue is empty, I'm gonna pick the track.",
		};
	}
}
