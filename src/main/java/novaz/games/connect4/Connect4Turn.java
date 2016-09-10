package novaz.games.connect4;

import novaz.games.GameTurn;

/**
 * Created on 2016-09-10.
 */
public class Connect4Turn extends GameTurn {
	private int columnIndex;

	public Connect4Turn(int boardIndex) {

		this.columnIndex = boardIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}
}