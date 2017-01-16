package discordbot.util;


import java.util.HashMap;
import java.util.Map;

public class Emojibet {
	private static final Map<String, String> emojis = new HashMap<>();
	private static final String UNKNOWN = " ";
	public static final String ID = "\uD83C\uDD94";
	public static final String MAN_IN_SUIT = "\uD83E\uDD35";
	public static final String MONKEY = "\uD83D\uDC12";
	public static final String INFORMATION = "\u2139";
	public static final String MONEY_BAG = "\uD83D\uDCB0";
	public static final String GAME_DICE = "\uD83C\uDFB2";
	public static final String CURRENCY_EXCHANGE = "\uD83D\uDCB1";
	public static final String SLOT_MACHINE = "\uD83C\uDFB0";
	public static final String FOOTPRINTS = "\uD83D\uDC3E";
	public static final String QUESTION_MARK = "\u2753";
	public static final String BOOK_OPEN = "\uD83D\uDCD6";
	public static final String DIAMOND_BLUE_SMALL = "\uD83D\uDD39";
	public static final String KEYBOARD = "\u2328";
	public static final String NOTEPAD = "\uD83D\uDDD2";
	public static final String GEAR = "\u2699";
	public static final String USER = "\uD83D\uDC64";
	public static final String GUILD_JOIN = "\uD83C\uDFE0";
	public static final String GUILD_LEAVE = "\uD83C\uDFDA";
	public static final String ERROR = "\uD83D\uDED1";
	public static final String WARNING = "\u26A0";
	public static final String MUSIC_NOTE = "\uD83C\uDFB5";
	public static final String KICKED = "\uD83E\uDD4A";
	public static final String BANNED = "\uD83D\uDD28";
	public static final String CALENDAR = "\uD83D\uDDD3";
	public static final String LOCKED = "\uD83D\uDD12";
	public static final String UNLOCKED = "\uD83D\uDD13";
	public static final String KEY = "\uD83D\uDD11";
	public static final String PREV_TRACK = "\u23EE";
	public static final String NEXT_TRACK = "\u23ED";
	public static final String STAR = "\u2B50";
	public static final String THUMBS_UP = "\uD83D\uDC4D";
	public static final String THUMBS_DOWN = "\uD83D\uDC4E";
	public static final String THUMBS_LEFT = "\uD83D\uDC48";
	public static final String THUMBS_RIGHT = "\uD83D\uDC49";//yes thumbs right
	public static final String COOKIE = "\uD83C\uDF6A";
	public static final String NO_ENTRY = "\uD83D\uDEAB";
	public static final String OKE_SIGN = "\u2705";
	public static final String SHARD_ICON = "\uD83D\uDC8E";
	public static final String POLICE = "\uD83D\uDE94";
	public static final String FIRE = "\uD83D\uDD25";
	public static final String FIRE_TRUCK = "\uD83D\uDE92";
	public static final String WATER_DROP = "\uD83D\uDCA7";
	public static final String ROCKET = "\uD83D\uDE80";
	public static final String X = "\u274C";

	public static final String ANGER = "\uD83D\uDCA2";
	public static final String EYES = "\uD83D\uDC40";
	public static final String SHRUG = "\uD83E\uDD37";
	public static final String CHECK_BOX = "\u2611";
	public static final String WRENCH = "\uD83D\uDD27";
	public static final String BUG = "\uD83D\uDC1B";
	public static final String BOOT = "\uD83D\uDC62";
	public static final String BASKET = "\uD83D\uDDD1";

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
