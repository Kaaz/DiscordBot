package novaz.games;

import novaz.games.blackjack.BlackJackHand;
import novaz.games.card.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Blackjack {

	BlackJackHand dealer;
	Map<String, BlackJackHand> playerHands;
	ArrayList<Card> deck;

	public Blackjack() {

		resetGame();
	}

	public String printHand(String player) {
		if (playerHands.containsKey(player)) {
			return playerHands.get(player).printHand();
		}
		return "";
	}

	public int getValue(String player) {
		if (playerHands.containsKey(player)) {
			return playerHands.get(player).getValue();
		}
		return 0;
	}

	private Card drawCard() {
		return deck.remove(0);
	}

	public void hit(String player) {
		if (!playerHands.containsKey(player)) {
			playerHands.put(player, new BlackJackHand());
		}
		if (playerHands.get(player).getValue() == 0) {
			playerHands.get(player).add(drawCard());
		}
		playerHands.get(player).add(drawCard());
	}

	public void resetGame() {

		dealer = new BlackJackHand();
		playerHands = new ConcurrentHashMap<>();
		deck = Card.newDeck();
		Collections.shuffle(deck);
	}
}
