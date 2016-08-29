package novaz.games.tictactoe;

public class TicTile {
	private TileState state;
	int player;

	public TicTile() {
		reset();
	}

	public void setPlayer(int player) {
		switch (player) {
			case 1:
				this.player = player;
				state = TileState.X;
				break;
			case 2:
				this.player = player;
				state = TileState.O;
				break;
			default:
				player = 0;
				state = TileState.FREE;
		}
	}

	private void reset() {
		state = TileState.FREE;
		player = 0;
	}

	public boolean isFree() {
		return state.equals(TileState.FREE);
	}

	public void setState(TileState state) {
		this.state = state;
	}

	public TileState getState() {
		return state;
	}

	public int getPlayer() {
		return player;
	}
}
