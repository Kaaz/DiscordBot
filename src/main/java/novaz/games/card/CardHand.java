package novaz.games.card;

import java.util.ArrayList;
import java.util.List;

public class CardHand {

	protected ArrayList<Card> cardsInHand;

	public CardHand() {
		reset();
	}

	public void reset() {
		cardsInHand = new ArrayList<>();
	}

	public void add(Card card) {
		cardsInHand.add(card);
	}

	public boolean remove(Card card) {
		return cardsInHand.remove(card);
	}

	public List<Card> getHand() {
		return cardsInHand;
	}
}