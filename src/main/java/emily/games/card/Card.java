/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.games.card;

import java.util.ArrayList;
import java.util.List;

public class Card {
    private static final List<Card> protoDeck = new ArrayList<>();

    static {
        for (CardSuit suit : CardSuit.values()) {
            for (CardRank rank : CardRank.values()) {
                protoDeck.add(new Card(rank, suit));
            }
        }
    }

    private final CardRank rank;
    private final CardSuit suit;

    private Card(CardRank rank, CardSuit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public static ArrayList<Card> newDeck() {
        return new ArrayList<>(protoDeck);
    }

    public CardRank getRank() {
        return rank;
    }

    public CardSuit getSuit() {
        return suit;
    }

    public String toString() {
        return rank.getDisplayName() + " of " + suit.getDisplayName();
    }

    public String toEmote() {
        return "[" + suit.getEmoticon() + rank.getEmoticon() + "]";
    }
}