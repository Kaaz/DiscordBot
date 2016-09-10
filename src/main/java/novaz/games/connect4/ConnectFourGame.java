package novaz.games.connect4;

import novaz.games.AbstractGame;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created on 9-9-2016
 */
public class ConnectFourGame extends AbstractGame<C4Turn> {

	public ConnectFourGame() {
		reset();
	}

	public void reset() {
		super.reset();
	}

	@Override
	public int getTotalPlayers() {
		return 2;
	}

	@Override
	protected boolean isTheGameOver() {
		return false;
	}

	@Override
	public boolean isValidMove(IUser player, C4Turn turnInfo) {
		return false;
	}

	@Override
	protected void doPlayerMove(IUser player, C4Turn turnInfo) {

	}
}
