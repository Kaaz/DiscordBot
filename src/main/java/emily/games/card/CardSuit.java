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

public enum CardSuit {
    CLUBS("clubs", ":clubs:"),
    DIAMONDS("diamonds", ":diamonds:"),
    HEARTS("hearts", ":hearts:"),
    SPADES("spades", ":spades:");
    private final String displayName;
    private final String emoticon;

    CardSuit(String displayName, String emoticon) {

        this.displayName = displayName;
        this.emoticon = emoticon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoticon() {
        return emoticon;
    }
}