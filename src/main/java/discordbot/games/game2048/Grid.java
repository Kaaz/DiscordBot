package discordbot.games.game2048;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Random;

public class Grid {

	private int[][] board;
	private final int size;
	private final Random rng;

	public Grid(int boardSize) {
		size = boardSize;
		rng = new Random();
		board = new int[size][size];
		for (int i = 0; i < size; i++) {
			board[i] = new int[size];
			for (int j = 0; j < size; j++) {
				board[i][j] = 0;
			}
		}
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

	public Pair<Integer, Integer> randomFreePosition() {
		if (isBoardFull()) {
			return null;
		}
		ArrayList<Pair<Integer, Integer>> availablePositions = getAvailablePositions();
		return availablePositions.get(rng.nextInt(availablePositions.size()));
	}
}