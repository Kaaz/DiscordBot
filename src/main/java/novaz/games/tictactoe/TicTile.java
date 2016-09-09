package novaz.games.tictactoe;

public class TicTile {
	private int player;
	private TileState state;

	public TicTile() {
		reset();
	}

	private void reset() {
		state = TileState.FREE;
		player = -1;
	}

	public boolean isFree() {
		return state.equals(TileState.FREE);
	}

	public TileState getState() {
		return state;
	}

	public void setState(TileState state) {
		this.state = state;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		switch (player) {
			case 0:
				this.player = player;
				state = TileState.X;
				break;
			case 1:
				this.player = player;
				state = TileState.O;
				break;
			default:
				this.player = -1;
				state = TileState.FREE;
		}
	}
}
