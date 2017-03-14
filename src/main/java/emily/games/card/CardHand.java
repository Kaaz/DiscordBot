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