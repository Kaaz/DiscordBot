package discordbot.games.connect4;

import discordbot.games.AbstractGame;
import discordbot.games.GameState;
import discordbot.main.Config;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.User;

/**
 * Created on 9-9-2016
 */
public class ConnectFourGame extends AbstractGame<Connect4Turn> {

	public static final int ROWS = 6, COLS = 7;
	private C4Board board;

	public ConnectFourGame() {
		reset();
	}

	public void reset() {
		super.reset();
		board = new C4Board(COLS, ROWS);
	}

	@Override
	public String getCodeName() {
		return "cf";
	}

	@Override
	public String[] getReactions() {
		return new String[]{
				"1", "2", "3", "4", "5", "6", "7", "8", "9"
		};
	}

	@Override
	public String getFullname() {
		return "Connect Four";
	}

	@Override
	public int getTotalPlayers() {
		return 2;
	}

	@Override
	protected boolean isTheGameOver() {

		for (int j = 0; j < ROWS - 3; j++) {
			for (int i = 0; i < COLS; i++) {
				if (this.board.getValue(i, j) == getActivePlayerIndex() &&
						this.board.getValue(i, j + 1) == getActivePlayerIndex() &&
						this.board.getValue(i, j + 2) == getActivePlayerIndex() &&
						this.board.getValue(i, j + 3) == getActivePlayerIndex()) {
					setWinner(getActivePlayerIndex());
					return true;
				}
			}
		}
		// verticalCheck
		for (int i = 0; i < COLS - 3; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (this.board.getValue(i, j) == getActivePlayerIndex() &&
						this.board.getValue(i + 1, j) == getActivePlayerIndex() &&
						this.board.getValue(i + 2, j) == getActivePlayerIndex() &&
						this.board.getValue(i + 3, j) == getActivePlayerIndex()) {
					setWinner(getActivePlayerIndex());
					return true;
				}
			}
		}
		// ascendingDiagonalCheck
		for (int i = 3; i < COLS; i++) {
			for (int j = 0; j < ROWS - 3; j++) {
				if (this.board.getValue(i, j) == getActivePlayerIndex() &&
						this.board.getValue(i - 1, j + 1) == getActivePlayerIndex() &&
						this.board.getValue(i - 2, j + 2) == getActivePlayerIndex() &&
						this.board.getValue(i - 3, j + 3) == getActivePlayerIndex()) {
					setWinner(getActivePlayerIndex());
					return true;
				}
			}
		}
		// descendingDiagonalCheck
		for (int i = 3; i < COLS; i++) {
			for (int j = 3; j < ROWS; j++) {
				if (this.board.getValue(i, j) == getActivePlayerIndex() &&
						this.board.getValue(i - 1, j - 1) == getActivePlayerIndex() &&
						this.board.getValue(i - 2, j - 2) == getActivePlayerIndex() &&
						this.board.getValue(i - 3, j - 3) == getActivePlayerIndex()) {
					setWinner(getActivePlayerIndex());
					return true;
				}
			}
		}
		for (int i = 0; i < COLS; i++) {
			if (board.canPlaceInColumn(i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isValidMove(User player, Connect4Turn turnInfo) {
		return board.canPlaceInColumn(turnInfo.getColumnIndex());
	}

	@Override
	protected void doPlayerMove(User player, Connect4Turn turnInfo) {
		board.placeInColumn(turnInfo.getColumnIndex(), getActivePlayerIndex());
	}

	@Override
	public String toString() {
		String ret = "A Connect 4 game." + Config.EOL;
		ret += board.toString();
		for (int i = 0; i < COLS; i++) {
			if (board.canPlaceInColumn(i)) {
				ret += Misc.numberToEmote(i + 1);
			} else {
				ret += Emojibet.NO_ENTRY;
			}
		}
		ret += Config.EOL + Config.EOL;
		if (getGameState().equals(GameState.IN_PROGRESS) || getGameState().equals(GameState.READY)) {
			ret += board.intToPlayer(0) + " = " + getPlayer(0).getName() + Config.EOL;
			ret += board.intToPlayer(1) + " = " + getPlayer(1).getName() + Config.EOL;
			ret += "It's the turn of " + getActivePlayer().getAsMention() + Config.EOL;
			ret += "to play type **" + getLastPrefix() + "game <columnnumber>**";
		}
		if (getGameState().equals(GameState.OVER)) {
			if (getWinnerIndex() == getTotalPlayers()) {
				ret += "Its over! And its a draw!";
			} else {
				ret += "Its over! The winner is " + getPlayer(getWinnerIndex()).getAsMention();
			}
		}
		return ret;
	}
}
