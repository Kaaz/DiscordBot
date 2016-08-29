package novaz.games;

import novaz.games.tictactoe.TicTile;
import novaz.games.tictactoe.TileState;
import novaz.main.Config;
import sx.blah.discord.handle.obj.IUser;

public class TicTacToe {
	private static final int TILES_ON_BOARD = 9;
	private static final int PLAYERS_IN_GAME = 2;

	private IUser[] players = new IUser[PLAYERS_IN_GAME];
	private TicTile[] board = new TicTile[TILES_ON_BOARD];
	private int currentPlayer;
	private GameState gameState;

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

	public TicTacToe() {
		reset();
	}

	/**
	 * resets the board
	 */
	private void reset() {
		for (int i = 0; i < TILES_ON_BOARD; i++) {
			board[i] = new TicTile();
		}
		for (int i = 0; i < PLAYERS_IN_GAME; i++) {
			players[i] = null;
		}
		currentPlayer = 0;
		gameState = GameState.INITIALIZING;
	}

	/**
	 * adds a player to the game
	 *
	 * @param player the player
	 * @return if it added the player to the game or not
	 */
	public boolean addPlayer(IUser player) {
		for (int i = 0; i < PLAYERS_IN_GAME; i++) {
			if (players[i] == null) {
				players[i] = player;
				return true;
			}
		}
		if (players[PLAYERS_IN_GAME - 1] != null) {
			gameState = GameState.READY;
		}
		return false;
	}

	public void doTurn(IUser player, int boardIndex) {
		if (isValidMove(player, boardIndex)) {
			gameState = GameState.IN_PROGRESS;
			board[boardIndex].setPlayer(currentPlayer);
		}
	}

	public boolean isTurnOf(IUser player) {
		return players[currentPlayer].equals(player);
	}

	public boolean isValidMove(IUser player, int boardIndex) {
		return players[currentPlayer].equals(player) && boardIndex < TILES_ON_BOARD && board[boardIndex].isFree();
	}

	/**
	 * checks if a player has won
	 *
	 * @return index of winner, -1 if there is no winner
	 */
	public int getWinner() {
		for (int[] combo : winCombos) {
			if (!board[combo[0]].isFree() && board[combo[0]].getPlayer() == board[combo[1]].getPlayer() && board[combo[1]].getPlayer() == board[combo[2]].getPlayer()) {
				gameState = GameState.OVER;
				return board[combo[0]].getPlayer();
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder game = new StringBuilder();
		for (int i = 0; i < TILES_ON_BOARD; i++) {
			game.append(board[i].getState().getEmoticon());
			if (i + 1 % 3 == 0) {
				game.append(Config.EOL);
			}
		}
		game.append(Config.EOL);
		if (gameState.equals(GameState.INITIALIZING)) {
			game.append("Waiting for another player!").append(Config.EOL);
		}
		if (gameState.equals(GameState.IN_PROGRESS) || gameState.equals(GameState.READY)) {
			game.append(TileState.X.getEmoticon()).append(" = ").append(players[0].getName()).append(Config.EOL);
			game.append(TileState.O.getEmoticon()).append(" = ").append(players[1].getName()).append(Config.EOL);
			game.append("It's the turn of ").append(players[currentPlayer].mention()).append(Config.EOL);
		}
		if (gameState.equals(GameState.OVER)) {
			game.append("Its over! The winner is ").append(players[getWinner()].mention());
		}
		return game.toString();
	}
}