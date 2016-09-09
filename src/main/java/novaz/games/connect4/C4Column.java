package novaz.games.connect4;

import novaz.games.GamePlayer;

/**
 * Created on 9-9-2016
 */
public class C4Column {

	private int spaceAvailable;
	private GamePlayer[] column;

	C4Column(int size) {
		this.spaceAvailable = size;
		column = new GamePlayer[size];
		for (int i = 0; i < column.length; i++) {
			column[i] = GamePlayer.FREE;
		}
	}

	public boolean place(GamePlayer player) {
		if (hasSpace()) {
			column[spaceAvailable - 1] = player;
		}
		return false;
	}

	public boolean hasSpace() {
		return spaceAvailable == 0;
	}
}
