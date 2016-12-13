package discordbot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class MusicUtil {
	private final static String BLOCK_INACTIVE = "\u25AC";
	private final static String BLOCK_ACTIVE = "\uD83D\uDD18";
	private final static String SOUND_CHILL = "\uD83D\uDD09";
	private final static String SOUND_LOUD = "\uD83D\uDD0A";
	private final static float SOUND_TRESHHOLD = 0.4F;
	private final static int BLOCK_PARTS = 10;

	public static MessageEmbed nowPlayingMessage() {

		return new EmbedBuilder().build();
	}

	public static String nowPlayingMessageNoEmbed() {

		return "";
	}


	/**
	 * @param startTime timestamp (in seconds) of the moment the song started playing
	 * @param duration  current song length in seconds
	 * @param volume    volume of the player
	 * @return a formatted mediaplayer
	 */
	public static String getMediaplayerProgressbar(long startTime, long duration, float volume, boolean isPaused) {
		long current = System.currentTimeMillis() / 1000 - startTime;
		String bar = isPaused ? "\u23EF" : "\u23F8 ";
		int activeBLock = (int) ((float) current / (float) duration * (float) BLOCK_PARTS);
		for (int i = 0; i < BLOCK_PARTS; i++) {
			if (i == activeBLock) {
				bar += BLOCK_ACTIVE;
			} else {
				bar += BLOCK_INACTIVE;
			}
		}
		bar += " [" + Misc.getDurationString(current) + "/" + Misc.getDurationString(duration) + "] ";
		if (volume >= SOUND_TRESHHOLD) {
			bar += SOUND_LOUD;
		} else {
			bar += SOUND_CHILL;
		}
		return bar;
	}
}
