package discordbot.games.gameofsticks;

import discordbot.games.GameTurn;

public class GoSTurn extends GameTurn {
	private int substract = 0;

	public GoSTurn() {

	}

	public GoSTurn(int boardIndex) {

		this.substract = boardIndex;
	}

	public int getSubstract() {
		return substract;
	}

	@Override
	public boolean parseInput(String input) {
		if (input != null && input.matches("^[1-3]$")) {
			this.substract = Integer.parseInt(input);
			return true;
		}
		return false;
	}

	@Override
	public String getInputErrorMessage() {
		return "Expecting a numeric input in range 1-3";
	}
}
