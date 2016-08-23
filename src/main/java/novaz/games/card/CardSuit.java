package novaz.games.card;

public enum CardSuit {
	CLUBS("clubs", ":clubs:"),
	DIAMONDS("diamonds", ":diamonds:"),
	HEARTS("hearts", ":hearts:"),
	SPADES("spades", ":spades:");
	private final String displayName;
	private final String emoticon;

	CardSuit(String displayName, String emoticon) {

		this.displayName = displayName;
		this.emoticon = emoticon;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getEmoticon() {
		return emoticon;
	}
}