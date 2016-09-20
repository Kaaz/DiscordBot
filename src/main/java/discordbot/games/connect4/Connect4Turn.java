package discordbot.games.connect4;

import discordbot.games.GameTurn;

/**
 * Created on 2016-09-10.
 */
public class Connect4Turn extends GameTurn {
	private int columnIndex;

	public Connect4Turn() {

	}

	public Connect4Turn(int boardIndex) {

		this.columnIndex = boardIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	@Override
	public boolean parseInput(String input) {
		if (input != null && input.matches("^[1-7]$")) {
			this.columnIndex = Integer.parseInt(input) - 1;
			return true;
		}
		return false;
	}

	@Override
	public String getInputErrorMessage() {
		return "Expecting a numeric input between 1 and 7";
	}
}