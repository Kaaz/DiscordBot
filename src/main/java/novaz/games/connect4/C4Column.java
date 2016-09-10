package novaz.games.connect4;

/**
 * Created on 9-9-2016
 */
public class C4Column {

	private int spaceAvailable;
	private int[] column;

	C4Column(int size) {
		this.spaceAvailable = size;
		column = new int[size];
		for (int i = 0; i < column.length; i++) {
			column[i] = -1;
		}
	}

	public boolean place(int player) {
		if (hasSpace()) {
			column[spaceAvailable - 1] = player;
			spaceAvailable--;
		}
		return false;
	}

	public boolean hasSpace() {
		return spaceAvailable > 0;
	}

	public int getCol(int colindex) {
		return column[colindex];
	}
}
