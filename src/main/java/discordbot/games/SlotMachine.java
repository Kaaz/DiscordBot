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

package discordbot.games;

import discordbot.games.slotmachine.Slot;
import discordbot.main.Config;

import java.util.Random;

public class SlotMachine {
	public final static String emptySLotIcon = ":white_small_square:";
	private final Random rng;
	private final Slot[] slotOptions = Slot.values();
	private final int wheels;
	private final int[] results;
	private int currentWheel;
	private Slot winSlot = slotOptions[0];
	private int winSlotAmount = 0;
	private int winMultiplier = 0;

	public SlotMachine() {
		rng = new Random();
		wheels = 3;
		currentWheel = 0;
		results = new int[wheels];
	}

	public void spin() {
		if (currentWheel < wheels) {
			results[currentWheel] = rng.nextInt(slotOptions.length) + slotOptions.length;
			if (!gameInProgress()) {
				calculateWinnings();
			}
			currentWheel++;
		}
	}

	private void calculateWinnings() {
		if (results[0] == results[1] && results[1] == results[2]) {
			winSlot = slotOptions[results[0] % slotOptions.length];
			winMultiplier = slotOptions[results[0] % slotOptions.length].getTriplePayout();
			winSlotAmount = 3;
		} else if (results[0] == results[1]) {
			winSlot = slotOptions[results[0] % slotOptions.length];
			winMultiplier = slotOptions[results[0] % slotOptions.length].getDoublePayout();
			winSlotAmount = 2;
		} else if (results[1] == results[2]) {
			winSlot = slotOptions[results[1] % slotOptions.length];
			winMultiplier = slotOptions[results[1] % slotOptions.length].getDoublePayout();
			winSlotAmount = 2;
		} else if (results[0] == results[3]) {
			winSlot = slotOptions[results[0] % slotOptions.length];
			winMultiplier = slotOptions[results[0] % slotOptions.length].getDoublePayout();
			winSlotAmount = 2;
		} else {
			for (int result : results) {
				if (slotOptions[result % slotOptions.length].getSinglePayout() > 0) {
					winSlot = slotOptions[result % slotOptions.length];
					winMultiplier = slotOptions[result % slotOptions.length].getDoublePayout();
					winSlotAmount = 1;
					break;
				}
			}
		}
	}

	public Slot getWinSlot() {
		return winSlot;
	}

	public int getWinMultiplier() {
		return winMultiplier;
	}

	public boolean gameInProgress() {
		return wheels > currentWheel;
	}

	private String getIconForIndex(int i) {
		if (i <= 0) {
			return emptySLotIcon;
		}
		return slotOptions[i % slotOptions.length].getEmote();
	}

	@Override
	public String toString() {
		String table = "the slotmachine! " + Config.EOL;
		String[] machineLine = new String[wheels];
		for (int i = 0; i < wheels; i++) {
			machineLine[i] = "";
		}
		int totalrows = 3;
		for (int col = 0; col < wheels; col++) {
			int offset = -1;
			for (int row = 0; row < totalrows; row++) {
				if (results[col] > 0) {
					machineLine[row] += "|" + getIconForIndex(offset + results[col]);
				} else {
					machineLine[row] += "|" + getIconForIndex(results[col]);
				}
				offset++;
			}

		}
		for (int i = 0; i < wheels; i++) {
			table += machineLine[i] + "|" + Config.EOL;
		}
		return table;
	}

	public int getWinSlotAmount() {
		return winSlotAmount;
	}
}
