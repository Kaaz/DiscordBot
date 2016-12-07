package discordbot.games.tictactoe;

public enum TileState {
	X("\u274C"),
	O("\u2B55"),
	FREE("\u2754");

	private final String emoticon;

	TileState(String emoticon) {

		this.emoticon = emoticon;
	}

	public String getEmoticon() {
		return emoticon;
	}
}
