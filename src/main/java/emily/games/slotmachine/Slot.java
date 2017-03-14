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

package emily.games.slotmachine;

public enum Slot {
    SEVEN("Seven", ":seven:", 30, 4, 1),
    CROWN("Crown", ":crown:", 10),
    BELL("Bell", ":bell:", 10),
    BAR("Bar", ":chocolate_bar:", 10),
    CHERRY("Cherry", ":cherries:", 10),
    MELON("Melon", ":melon:", 10);

    private final String name;
    private final String emote;
    private final int triplePayout;
    private final int doublePayout;
    private final int singlePayout;

    Slot(String name, String emote, int triplePayout) {
        this(name, emote, triplePayout, 0, 0);
    }

    Slot(String name, String emote, int triplePayout, int doublePayout) {
        this(name, emote, triplePayout, doublePayout, 0);
    }

    Slot(String name, String emote, int triplePayout, int doublePayout, int singlePayout) {

        this.name = name;
        this.emote = emote;
        this.triplePayout = triplePayout;
        this.doublePayout = doublePayout;
        this.singlePayout = singlePayout;
    }

    public int getTriplePayout() {
        return triplePayout;
    }

    public String getEmote() {
        return emote;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return emote;
    }

    public int getDoublePayout() {
        return doublePayout;
    }

    public int getSinglePayout() {
        return singlePayout;
    }
}
