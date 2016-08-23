package novaz.games.card;

import java.util.ArrayList;
import java.util.List;

public class Card {
	private final CardRank rank;
	private final CardSuit suit;

	private Card(CardRank rank, CardSuit suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public CardRank rank() {
		return rank;
	}

	public CardSuit suit() {
		return suit;
	}

	public String toString() {
		return rank.getDisplayName() + " of " + suit.getDisplayName();
	}

	public String toEmote() {
		return suit.getEmoticon() + rank.getEmoticon();
	}

	private static final List<Card> protoDeck = new ArrayList<Card>();

	static {
		for (CardSuit suit : CardSuit.values()) {
			for (CardRank rank : CardRank.values()) {
				protoDeck.add(new Card(rank, suit));
			}
		}
	}

	public static ArrayList<Card> newDeck() {
		return new ArrayList<>(protoDeck);
	}
}