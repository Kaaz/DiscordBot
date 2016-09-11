package novaz.games.connect4;

import novaz.main.Config;

public class C4Board {

	private int spaceAvailable;
	private C4Column[] cols;
	private int columnSize;

	public C4Board(int columns, int rows) {
		cols = new C4Column[columns];
		for (int i = 0; i < cols.length; i++) {
			cols[i] = new C4Column(rows);
		}
		this.columnSize = rows;
	}

	public int getValue(int column, int row) {
		return cols[column].getCol(row);
	}

	/**
	 * @param index the column to place it in
	 * @return can the player place it?
	 */
	public boolean canPlaceInColumn(int index) {
		return cols[index] != null && cols[index].hasSpace();
	}

	/**
	 * @param index  the column
	 * @param player playerindex
	 * @return success
	 */
	public boolean placeInColumn(int index, int player) {
		return cols[index].place(player);
	}

	public String intToPlayer(int playerIndex) {
		switch (playerIndex) {
			case 0:
				return ":red_circle:";
			case 1:
				return ":large_blue_circle:";
		}
		return ":white_circle:";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int totalRows = cols.length;
		for (int height = 0; height < columnSize; height++) {
			for (C4Column col : cols) {
				sb.append(intToPlayer(col.getCol(height)));
			}
			sb.append(Config.EOL);
		}
		return sb.toString();
	}
}
