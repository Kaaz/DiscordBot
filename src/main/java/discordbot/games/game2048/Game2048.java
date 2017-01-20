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

	public void reset() {
		super.reset();
		grid = new Grid(GRID_SIZE);
	}

	@Override
	public String getCodeName() {
		return "2048";
	}

	@Override
	public String[] getReactions() {
		return new String[]{
				"up", "down", "left", "right"
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

		return false;
	}

	@Override
	public boolean isValidMove(User player, Game2048Turn turnInfo) {
		return !turnInfo.getDirection().equals(Game2048Direction.UNKNOWN);
	}

	@Override
	protected void doPlayerMove(User player, Game2048Turn turnInfo) {

	}

	@Override
	public String toString() {
		String ret = "A 2048 game, score as high as you can!." + Config.EOL;
		return ret;
	}
}
