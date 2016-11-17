package discordbot.util;


public enum Emojibet {

	A("a", "\uD83C\uDDE6"),
	B("b", "\uD83C\uDDE7"),
	C("c", "\uD83C\uDDE8"),
	D("d", "\uD83C\uDDE9"),
	E("e", "\uD83C\uDDEA"),
	F("f", "\uD83C\uDDEB"),
	G("g", "\uD83C\uDDEC"),
	H("h", "\uD83C\uDDED"),
	I("i", "\uD83C\uDDEE"),
	J("j", "\uD83C\uDDEF"),
	K("k", "\uD83C\uDDF0"),
	L("l", "\uD83C\uDDF1"),
	M("m", "\uD83C\uDDF2"),
	N("n", "\uD83C\uDDF3"),
	O("o", "\uD83C\uDDF4"),
	P("p", "\uD83C\uDDF5"),
	Q("q", "\uD83C\uDDF6"),
	R("r", "\uD83C\uDDF7"),
	S("s", "\uD83C\uDDF8"),
	T("t", "\uD83C\uDDF9"),
	U("u", "\uD83C\uDDFA"),
	V("v", "\uD83C\uDDFB"),
	W("w", "\uD83C\uDDFC"),
	X("x", "\uD83C\uDDFD"),
	Y("y", "\uD83C\uDDFE"),
	Z("z", "\uD83C\uDDFF"),
	SPACE(" ", "\u25AB"),
	UNKNOWN(" ", "\u25AB"),;

	private final String normal;
	private final String emoji;

	Emojibet(String normal, String emoji) {

		this.normal = normal;
		this.emoji = emoji;
	}

	public static String getEmoji(String alphachar) {
		return getEmoji(alphachar.toLowerCase().charAt(0));
	}

	public static String getEmoji(char alphachar) {
		if (" ".equals(alphachar)) {
			return SPACE.toString();
		}
		int index = alphachar - 97;
		if (index >= 0 && index < 26) {
			return values()[index].toString();
		}
		return UNKNOWN.toString();
	}

	public String getEmoji() {
		return emoji;
	}

	public String getAlpha() {
		return normal;
	}

	@Override
	public String toString() {
		return emoji;
	}
}
