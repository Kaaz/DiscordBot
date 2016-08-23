package novaz.games.card;

public enum CardRank {
	DEUCE("two", " 2", 2),
	THREE("tree", " 3", 3),
	FOUR("four", " 4", 4),
	FIVE("five", " 5", 5),
	SIX("six", " 6", 6),
	SEVEN("seven", " 7", 7),
	EIGHT("eight", " 8", 8),
	NINE("nine", " 9", 9),
	TEN("ten", "10", 10),
	JACK("jack", " J", 10),
	QUEEN("queen", " Q", 10),
	KING("king", " K", 10),
	ACE("ace", " A", 11);

	private String cardName;
	private String emoticon;
	private int value;

	CardRank(String cardName, String emoticon, int value) {
		this.cardName = cardName;

		this.emoticon = emoticon;
		this.value = value;
	}

	public String getEmoticon() {
		return emoticon;
	}

	public String getDisplayName() {
		return cardName;
	}

	public int getValue() {
		return value;
	}
}
