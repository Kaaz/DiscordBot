package novaz.games;

import novaz.games.slotmachine.Slot;
import novaz.main.Config;

import java.util.Random;

public class SlotMachine {
	private final Random rng;
	private final Slot[] slotOptions = Slot.values();
	public final static String emptySLotIcon = ":white_small_square:";
	private final int wheels;
	private final int[] results;
	private int currentWheel;

	public SlotMachine() {
		rng = new Random();
		wheels = 3;
		currentWheel = 0;
		results = new int[wheels];
	}

	public void spin() {
		if (currentWheel < wheels) {
			results[currentWheel] = rng.nextInt(slotOptions.length) + slotOptions.length;
			currentWheel++;
		}
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
			System.out.print(results[i] + " ");
			table += machineLine[i] + "|" + Config.EOL;
		}
		System.out.println("");
		return table;
	}
}
