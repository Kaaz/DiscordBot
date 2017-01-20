package discordbot.games.game2048;

import discordbot.games.GameTurn;

public class Game2048Turn extends GameTurn {
	private Game2048Direction direction = Game2048Direction.UNKNOWN;

	public Game2048Turn() {

	}

	public Game2048Direction getDirection() {
		return direction;
	}

	@Override
	public boolean parseInput(String input) {
		if (input != null) {
			this.direction = Game2048Direction.fromString(input);
			return direction.equals(Game2048Direction.UNKNOWN);
		}
		return false;
	}

	@Override
	public String getInputErrorMessage() {
		return "Expecting a direction: up, down, left, right (or u/d/l/r)";
	}
}
