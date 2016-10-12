package discordbot.games.tictactoe;

import discordbot.games.AbstractGame;
import discordbot.games.GameState;
import discordbot.games.GameTurn;
import discordbot.main.Config;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.User;
import sx.blah.discord.handle.obj.IUser;

public class TicTacToeGame extends AbstractGame<TicGameTurn> {
	private static final int TILES_ON_BOARD = 9;
	private final int[][] winCombos = {
			{0, 1, 2},
			{3, 4, 5},
			{6, 7, 8},
			{0, 3, 6},
			{1, 4, 7},
			{2, 5, 8},
			{0, 4, 8},
			{2, 4, 6}
	};
	private TicTile[] board = new TicTile[TILES_ON_BOARD];

	public TicTacToeGame() {
		reset();
	}

	/**
	 * resets the board
	 */
	public void reset() {
		super.reset();
		for (int i = 0; i < TILES_ON_BOARD; i++) {
			board[i] = new TicTile();
		}
	}

	@Override
	public String getCodeName() {
		return "tic";
	}

	@Override
	public String getFullname() {
		return "Tic tac toe";
	}

	@Override
	public int getTotalPlayers() {
		return 2;
	}

	@Override
	public boolean isValidMove(User player, GameTurn turnInfo) {
		return turnInfo.getBoardIndex() < TILES_ON_BOARD && board[turnInfo.getBoardIndex()].isFree();
	}

	@Override
	protected void doPlayerMove(IUser player, TicGameTurn turnInfo) {
		board[turnInfo.getBoardIndex()].setPlayer(getActivePlayerIndex());
	}

	@Override
	protected boolean isTheGameOver() {
		for (int[] combo : winCombos) {
			if (board[combo[0]].isFree()) {
				continue;
			}
			if (board[combo[0]].getPlayer() == board[combo[1]].getPlayer() && board[combo[1]].getPlayer() == board[combo[2]].getPlayer()) {
				setWinner(board[combo[0]].getPlayer());
				return true;
			}
		}
		for (TicTile tt : board) {
			if (tt.isFree()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder game = new StringBuilder();
		game.append("Game of Tic").append(Config.EOL);
		for (int i = 0; i < TILES_ON_BOARD; i++) {
			if (board[i].getState().equals(TileState.FREE)) {
				game.append(Misc.numberToEmote(i + 1));
			} else {
				game.append(board[i].getState().getEmoticon());
			}
			if ((i + 1) % 3 == 0) {
				game.append(Config.EOL);
			}
		}
		game.append(Config.EOL);
		if (getGameState().equals(GameState.INITIALIZING)) {
			game.append("Waiting for another player!").append(Config.EOL);
		}
		if (getGameState().equals(GameState.IN_PROGRESS) || getGameState().equals(GameState.READY)) {
			game.append(TileState.X.getEmoticon()).append(" = ").append(getPlayer(0).getName()).append(Config.EOL);
			game.append(TileState.O.getEmoticon()).append(" = ").append(getPlayer(1).getName()).append(Config.EOL);
			game.append("It's the turn of ").append(getActivePlayer().mention()).append(Config.EOL);
			game.append("to play type **game <number>**");
		}
		if (getGameState().equals(GameState.OVER)) {
			if (getWinnerIndex() == getTotalPlayers()) {
				game.append("Its over! And its a draw!");
			} else {
				game.append("Its over! The winner is ").append(getPlayer(getWinnerIndex()).mention());
			}
		}
		return game.toString();
	}
}