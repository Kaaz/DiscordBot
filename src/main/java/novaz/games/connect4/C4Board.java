package novaz.games.connect4;

import novaz.games.GameState;

public class C4Board {

	private int spaceAvailable;
	private C4Column[] cols;
	private GameState gamestate;

	public C4Board(int columns, int rows) {
		gamestate = GameState.INITIALIZING;
		cols = new C4Column[columns];
		for (int i = 0; i < cols.length; i++) {
			cols[i] = new C4Column(rows);
		}
	}

	/**
	 * @param index the column to place it in
	 * @return can the player place it?
	 */
	public boolean canPlaceInColumn(int index) {
		if (index - 1 < cols.length) {
			cols[index].hasSpace();
		}
		return false;
	}
}
