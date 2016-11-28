package discordbot.util;


import java.util.HashMap;
import java.util.Map;

public class Emojibet {
	private static final Map<String, String> emojis = new HashMap<>();
	private static final String UNKNOWN = " ";
	public static String GUILD_JOIN = "\uD83C\uDFE0";
	public static String GUILD_LEAVE = "\uD83C\uDFDA";
	public static String ERROR = "\uD83D\uDED1";
	public static String WARNING = "\u26A0";
	public static String KICKED = "\uD83E\uDD4A";
	public static String BANNED = "\uD83D\uDD28";
	public static String CALENDAR = "\uD83D\uDDD3";
	public static String LOCKED = "\uD83D\uDD12";
	public static String UNLOCKED = "\uD83D\uDD13";
	public static String KEY = "\uD83D\uDD11";
	public static String NEXT_TRACK = "\u23ED";


	static {
		emojis.put("a", "\uD83C\uDDE6");
		emojis.put("b", "\uD83C\uDDE7");
		emojis.put("c", "\uD83C\uDDE8");
		emojis.put("d", "\uD83C\uDDE9");
		emojis.put("e", "\uD83C\uDDEA");
		emojis.put("f", "\uD83C\uDDEB");
		emojis.put("g", "\uD83C\uDDEC");
		emojis.put("h", "\uD83C\uDDED");
		emojis.put("i", "\uD83C\uDDEE");
		emojis.put("j", "\uD83C\uDDEF");
		emojis.put("k", "\uD83C\uDDF0");
		emojis.put("l", "\uD83C\uDDF1");
		emojis.put("m", "\uD83C\uDDF2");
		emojis.put("n", "\uD83C\uDDF3");
		emojis.put("o", "\uD83C\uDDF4");
		emojis.put("p", "\uD83C\uDDF5");
		emojis.put("q", "\uD83C\uDDF6");
		emojis.put("r", "\uD83C\uDDF7");
		emojis.put("s", "\uD83C\uDDF8");
		emojis.put("t", "\uD83C\uDDF9");
		emojis.put("u", "\uD83C\uDDFA");
		emojis.put("v", "\uD83C\uDDFB");
		emojis.put("w", "\uD83C\uDDFC");
		emojis.put("x", "\uD83C\uDDFD");
		emojis.put("y", "\uD83C\uDDFE");
		emojis.put("z", "\uD83C\uDDFF");
		emojis.put("0", "\u0030");
		emojis.put("1", "\u0031");
		emojis.put("2", "\u0032");
		emojis.put("3", "\u0033");
		emojis.put("4", "\u0034");
		emojis.put("5", "\u0035");
		emojis.put("6", "\u0036");
		emojis.put("7", "\u0037");
		emojis.put("8", "\u0038");
		emojis.put("9", "\u0039");
		emojis.put("?", "\u2754");
		emojis.put("!", "\u2755");
		emojis.put(" ", "\u25AB");
	}

	/**
	 * emoji for single character
	 *
	 * @param character the character to look for
	 * @return emoji version, or UNKNOWN
	 */
	public static String getEmojiFor(String character) {
		if (emojis.containsKey(character)) {
			return emojis.get(character);
		}
		return UNKNOWN;
	}

}
