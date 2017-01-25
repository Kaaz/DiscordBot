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

package discordbot.games.slotmachine;

public enum Slot {
	SEVEN("Seven", ":seven:", 100),
	CROWN("Crown", ":crown:", 50),
	BELL("Bell", ":bell:", 25),
	BAR("Bar", ":chocolate_bar:", 20),
	CHERRY("Cherry", ":cherries:", 15),
	MELON("Melon", ":melon:", 10);

	private final String name;
	private final String emote;
	private final int triplePayout;

	Slot(String name, String emote, int triplePayout) {

		this.name = name;
		this.emote = emote;
		this.triplePayout = triplePayout;
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
}
