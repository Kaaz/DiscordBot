package discordbot.games.game2048;

public enum Game2048Direction {
	UP(),
	RIGHT(),
	LEFT(),
	DOWN(),
	UNKNOWN();

	public static Game2048Direction fromString(String direction) {
		if (direction == null) {
			return UNKNOWN;
		}
		switch (direction.toLowerCase()) {
			case "up":
			case "u":
				return UP;
			case "right":
			case "r":
				return RIGHT;
			case "down":
			case "d":
				return DOWN;
			case "left":
			case "l":
				return LEFT;
		}
		return UNKNOWN;
	}

}
