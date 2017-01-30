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

package discordbot.games.card;

public enum CardRank {
    DEUCE("two", " 2", 2),
    THREE("tree", " 3", 3),
    FOUR("four", " 4", 4),
    FIVE("five", " 5", 5),
    SIX("six", " 6", 6),
    SEVEN("seven", " 7", 7),
    EIGHT("eight", " 8", 8),
    NINE("nine", " 9", 9),
    TEN("ten", "10", 10),
    JACK("jack", " J", 10),
    QUEEN("queen", " Q", 10),
    KING("king", " K", 10),
    ACE("ace", " A", 11);

    private String cardName;
    private String emoticon;
    private int value;

    CardRank(String cardName, String emoticon, int value) {
        this.cardName = cardName;

        this.emoticon = emoticon;
        this.value = value;
    }

    public String getEmoticon() {
        return emoticon;
    }

    public String getDisplayName() {
        return cardName;
    }

    public int getValue() {
        return value;
    }
}
