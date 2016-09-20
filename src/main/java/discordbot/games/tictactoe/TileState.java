package discordbot.games.tictactoe;

public enum TileState {
	X(":x:"),
	O(":o:"),
	FREE(":grey_question:");

	private final String emoticon;

	TileState(String emoticon) {

		this.emoticon = emoticon;
	}

	public String getEmoticon() {
		return emoticon;
	}
}
