package novaz.games.connect4;

import novaz.games.GameTurn;

/**
 * Created on 2016-09-10.
 */
public class C4Turn extends GameTurn {
	private int columnIndex;

	public C4Turn(int boardIndex) {

		this.columnIndex = boardIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}
}