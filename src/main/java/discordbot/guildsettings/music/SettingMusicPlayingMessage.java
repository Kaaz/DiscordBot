package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.types.EnumSettingType;


public class SettingMusicPlayingMessage extends AbstractGuildSetting<EnumSettingType> {
	@Override
	protected EnumSettingType getSettingsType() {
		return new EnumSettingType("clear", "normal", "off");
	}

	@Override
	public String getKey() {
		return "music_playing_message";
	}

	@Override
	public String getDefault() {
		return "clear";
	}

	@Override
	public String[] getDescription() {
		return new String[]{"Clear the now playing message?",
				"clear  -> sends a message and deletes it when the song is over or skipped",
				"normal -> send the message and just leave it be",
				"off    -> don't send now playing messages",
		};
	}
}
