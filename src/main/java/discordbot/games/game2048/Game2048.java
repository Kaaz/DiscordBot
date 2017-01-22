package discordbot.games.game2048;

import discordbot.games.AbstractGame;
import discordbot.main.Config;
import net.dv8tion.jda.core.entities.User;

/**
 *
 */
public class Game2048 extends AbstractGame<Game2048Turn> {

	public final int GRID_SIZE = 4;
	private Grid grid;

	public Game2048() {
		reset();
	}

	@Override
	public boolean isListed() {
		return false;
	}

	public void reset() {
		super.reset();
		grid = new Grid(GRID_SIZE);
		grid.addRandomTwo();
	}

	@Override
	public String getCodeName() {
		return "2048";
	}

	@Override
	public String[] getReactions() {
		return new String[]{
				"left", "up", "down", "right"
		};
	}

	@Override
	public String getFullname() {
		return "2048";
	}

	@Override
	public int getTotalPlayers() {
		return 1;
	}

	@Override
	protected boolean isTheGameOver() {
		return !grid.canMove(true) && !grid.canMove(false);
	}

	@Override
	public boolean isValidMove(User player, Game2048Turn turnInfo) {
		switch (turnInfo.getDirection()) {
			case LEFT:
			case RIGHT:
				return grid.canMove(true);
			case UP:
			case DOWN:
				return grid.canMove(false);
			default:
				return false;
		}
	}

	@Override
	protected void doPlayerMove(User player, Game2048Turn turnInfo) {
		switch (turnInfo.getDirection()) {
			case LEFT:
				grid.moveLeft();
				break;
			case RIGHT:
				grid.moveRight();
				break;
			case UP:
				grid.moveUp();
				break;
			case DOWN:
				grid.moveDown();
				break;
		}
		grid.addRandomTwo();
	}

	@Override
	public String toString() {
		String ret = "A 2048 game, score as high as you can!" + Config.EOL;
		ret += getPlayer(0).getAsMention() + "'s game\n";
		ret += "Score: " + grid.getScore() + " \n\n";
		String format = "`| %4s | %4s | %4s | %4s`\n";
		for (int i = 0; i < GRID_SIZE; i++) {
			ret += String.format(format, (Object[]) grid.board[i]);
		}
		return ret;
	}
}
