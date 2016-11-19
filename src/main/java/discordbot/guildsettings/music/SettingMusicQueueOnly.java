package discordbot.guildsettings.music;

import discordbot.guildsettings.AbstractGuildSetting;


public class SettingMusicQueueOnly extends AbstractGuildSetting {
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

	@Override
	public boolean isValidValue(String input) {
		return input != null && (input.equals("true") || input.equals("false"));
	}
}
