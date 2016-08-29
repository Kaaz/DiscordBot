package novaz.games;

import novaz.games.tictactoe.TicTile;
import novaz.games.tictactoe.TileState;
import novaz.main.Config;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IUser;

import java.util.Random;

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

	public boolean waitingForPlayer() {
		return gameState.equals(GameState.INITIALIZING);
	}

	/**
	 * adds a player to the game
	 *
	 * @param player the player
	 * @return if it added the player to the game or not
	 */
	public boolean addPlayer(IUser player) {
		if (!gameState.equals(GameState.INITIALIZING)) {
			return false;
		}
		for (int i = 0; i < PLAYERS_IN_GAME; i++) {
			if (players[i] == null) {
				players[i] = player;
				if (i == (PLAYERS_IN_GAME - 1)) {
					currentPlayer = new Random().nextInt(PLAYERS_IN_GAME);
					gameState = GameState.READY;
				}
				return true;
			}
		}
		return false;
	}

	public void doTurn(IUser player, int boardIndex) {
		if (isValidMove(player, boardIndex)) {
			gameState = GameState.IN_PROGRESS;
			board[boardIndex].setPlayer(currentPlayer);
			currentPlayer = (currentPlayer + 1) % PLAYERS_IN_GAME;
			getWinner();
		}
	}

	public boolean isTurnOf(IUser player) {
		return players[currentPlayer].equals(player);
	}

	public boolean isValidMove(IUser player, int boardIndex) {
		return !waitingForPlayer() && players[currentPlayer].equals(player) && boardIndex < TILES_ON_BOARD && board[boardIndex].isFree();
	}

	/**
	 * checks if a player has won
	 *
	 * @return index of winner, -1 if there is no winner, PLAYERS_IN_GAME if its a draw
	 */
	public int getWinner() {
		for (int[] combo : winCombos) {
			if (board[combo[0]].isFree()) {
				continue;
			}
			if (board[combo[0]].getPlayer() == board[combo[1]].getPlayer() && board[combo[1]].getPlayer() == board[combo[2]].getPlayer()) {
				gameState = GameState.OVER;
				return board[combo[0]].getPlayer();
			}
		}
		for (TicTile tt : board) {
			if (tt.isFree()) {
				return -1;
			}
		}
		return PLAYERS_IN_GAME;
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
		if (gameState.equals(GameState.INITIALIZING)) {
			game.append("Waiting for another player!").append(Config.EOL);
		}
		if (gameState.equals(GameState.IN_PROGRESS) || gameState.equals(GameState.READY)) {
			game.append(TileState.X.getEmoticon()).append(" = ").append(players[0].getName()).append(Config.EOL);
			game.append(TileState.O.getEmoticon()).append(" = ").append(players[1].getName()).append(Config.EOL);
			game.append("It's the turn of ").append(players[currentPlayer].mention()).append(Config.EOL);
			game.append("to play type **tic <number>**");
		}
		if (gameState.equals(GameState.OVER)) {
			if (getWinner() == PLAYERS_IN_GAME) {
				game.append("Its over! And its a draw!");
			} else {
				game.append("Its over! The winner is ").append(players[getWinner()].mention());
			}
		}
		return game.toString();
	}
}