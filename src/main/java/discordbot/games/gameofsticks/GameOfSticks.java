package discordbot.games.gameofsticks;

import discordbot.games.AbstractGame;
import discordbot.games.GameState;
import discordbot.main.Config;
import net.dv8tion.jda.core.entities.User;

/**
 * Created on 9-9-2016
 */
public class GameOfSticks extends AbstractGame<GoSTurn> {

	private int sticksleft;

	public GameOfSticks() {
		reset();
	}

	public void reset() {
		super.reset();
		sticksleft = 20;
	}

	@Override
	public String getCodeName() {
		return "gos";
	}

	@Override
	public String getFullname() {
		return "Game of sticks";
	}

	@Override
	public int getTotalPlayers() {
		return 2;
	}

	@Override
	protected boolean isTheGameOver() {
		if (sticksleft == 0) {
			setWinner(1 - getActivePlayerIndex());
			return true;
		}

		return false;
	}

	@Override
	public boolean isValidMove(User player, GoSTurn turnInfo) {
		return turnInfo.getSubstract() <= 3 && (sticksleft - turnInfo.getSubstract()) >= 0;
	}

	@Override
	protected void doPlayerMove(User player, GoSTurn turnInfo) {
		sticksleft -= turnInfo.getSubstract();
	}

	@Override
	public String toString() {
		String ret = "A Game of sticks." + Config.EOL;
		ret += "Take away a few sticks, the player to take away the last stick loses!" + Config.EOL;
		ret += "There are currently **" + sticksleft + "** Sticks left";
		ret += Config.EOL + Config.EOL;
		if (getGameState().equals(GameState.IN_PROGRESS) || getGameState().equals(GameState.READY)) {
			ret += "It's the turn of " + getActivePlayer().getAsMention() + Config.EOL;
			ret += "to play type **" + getLastPrefix() + "game 1-3**";
		} else if (getGameState().equals(GameState.OVER)) {
			ret += "Its over! The winner is " + getPlayer(getWinnerIndex()).getAsMention();
		}
		return ret;
	}
}
