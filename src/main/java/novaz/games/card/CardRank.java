package novaz.games.card;

public enum CardRank {
	DEUCE("two", " 2"),
	THREE("tree", " 3"),
	FOUR("four", " 4"),
	FIVE("five", " 5"),
	SIX("six", " 6"),
	SEVEN("seven", " 7"),
	EIGHT("eight", " 8"),
	NINE("nine", " 9"),
	TEN("ten", "10"),
	JACK("jack", " J"),
	QUEEN("queen", " Q"),
	KING("king", " K"),
	ACE("ace", " A");

	private String cardName;
	private String emoticon;

	CardRank(String cardName, String emoticon) {
		this.cardName = cardName;

		this.emoticon = emoticon;
	}

	public String getEmoticon() {
		return emoticon;
	}

	public String getDisplayName() {
		return cardName;
	}
}
