package discordbot.games.tictactoe;

import discordbot.games.GameTurn;

public class TicGameTurn extends GameTurn {
	private int boardIndex = 0;

	public TicGameTurn() {

	}

	public TicGameTurn(int boardIndex) {

		this.boardIndex = boardIndex;
	}

	public int getBoardIndex() {
		return boardIndex;
	}

	@Override
	public boolean parseInput(String input) {
		if (input != null && input.matches("^[1-9]$")) {
			this.boardIndex = Integer.parseInt(input) - 1;
			return true;
		}
		return false;
	}

	@Override
	public String getInputErrorMessage() {
		return "Expecting a numeric input in range 1-9";
	}
}
