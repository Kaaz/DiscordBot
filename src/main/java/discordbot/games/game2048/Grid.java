package discordbot.games.game2048;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Random;

public class Grid {

	public volatile Integer[][] board;
	private final int size;
	private final Random rng;

	public Grid(int boardSize) {
		size = boardSize;
		rng = new Random();
		board = getEmptyBoard();
	}

	public int getScore() {
		int sum = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				sum += board[i][j];
			}
		}
		return sum;
	}

	public boolean isBoardFull() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (board[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	private ArrayList<Pair<Integer, Integer>> getAvailablePositions() {
		ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (board[i][j] == 0) {
					list.add(Pair.of(i, j));
				}
			}
		}
		return list;
	}

	public void addRandomTwo() {
		Pair<Integer, Integer> pos = getRandomFreePosition();
		board[pos.getKey()][pos.getValue()] = 2;
	}

	public boolean canMoveHorizontal() {
		return canMove(true);
	}

	public boolean canMoveVertical() {
		return canMove(false);

	}

	public boolean canMove(boolean horizontal) {
		boolean hasFreeSpace = false;
		for (int i = 0; i < size; i++) {
			boolean rowSave = false;
			int lastVal = -1;
			for (int j = 0; j < size; j++) {
				if (board[horizontal ? i : j][horizontal ? j : i] == 0) {
					rowSave = true;
					hasFreeSpace = true;
				} else if (lastVal == board[horizontal ? i : j][horizontal ? j : i]) {
					hasFreeSpace = true;
					rowSave = true;
					break;
				}
				lastVal = board[horizontal ? i : j][horizontal ? j : i];
			}
			if (!rowSave) {
				return false;
			}
		}
		return hasFreeSpace;
	}

	public void moveRight() {
		Integer[][] tmp = getEmptyBoard();
		for (int i = 0; i < size; i++) {
			int index = size;
			int lastNumber = 0;
			for (int j = size - 1; j >= 0; j--) {
				if (board[i][j] == 0) {
					continue;
				}
				if (lastNumber != board[i][j] || lastNumber * 2 == tmp[i][index]) {
					index--;
				}
				tmp[i][index] += board[i][j];
				lastNumber = board[i][j];

			}
		}
		board = tmp;
	}

	public void moveLeft() {
		Integer[][] tmp = getEmptyBoard();
		for (int i = 0; i < size; i++) {
			int index = -1;
			int lastNumber = 0;
			for (int j = 0; j < size; j++) {
				if (board[i][j] == 0) {
					continue;
				}
				if (lastNumber != board[i][j] || lastNumber * 2 == tmp[i][index]) {
					index++;
				}
				tmp[i][index] += board[i][j];
				lastNumber = board[i][j];
			}
		}
		board = tmp;
	}

	public void moveUp() {
		Integer[][] tmp = getEmptyBoard();
		for (int i = 0; i < size; i++) {
			int index = -1;
			int lastNumber = 0;
			for (int j = 0; j < size; j++) {
				if (board[j][i] == 0) {
					continue;
				}
				if (lastNumber != board[j][i] || lastNumber * 2 == tmp[index][i]) {
					index++;
				}
				tmp[index][i] += board[j][i];
				lastNumber = board[j][i];

			}
		}
		board = tmp;
	}

	public void moveDown() {
		Integer[][] tmp = getEmptyBoard();
		for (int i = 0; i < size; i++) {
			int index = size;
			int lastNumber = 0;
			for (int j = size - 1; j >= 0; j--) {
				if (board[j][i] == 0) {
					continue;
				}
				if (lastNumber != board[j][i] || lastNumber * 2 == tmp[index][i]) {
					index--;
				}
				tmp[index][i] += board[j][i];
				lastNumber = board[j][i];
			}
		}
		board = tmp;
	}

	private Integer[][] getEmptyBoard() {
		Integer[][] board = new Integer[size][size];
		for (int i = 0; i < size; i++) {
			board[i] = new Integer[size];
			for (int j = 0; j < size; j++) {
				board[i][j] = 0;
			}
		}
		return board;
	}


	public Pair<Integer, Integer> getRandomFreePosition() {
		if (isBoardFull()) {
			return null;
		}
		ArrayList<Pair<Integer, Integer>> availablePositions = getAvailablePositions();
		return availablePositions.get(rng.nextInt(availablePositions.size()));
	}
}