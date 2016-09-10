package novaz.games.tictactoe;

import novaz.games.GameTurn;

public class TicGameTurn extends GameTurn {
	private int boardIndex;

	public TicGameTurn(int boardIndex) {

		this.boardIndex = boardIndex;
	}

	public int getBoardIndex() {
		return boardIndex;
	}
}
