package novaz.util;

import novaz.main.Config;

import java.util.List;
import java.util.StringJoiner;

public class Misc {

	/**
	 * @param items items in the table
	 * @return formatted table
	 */
	public static String makeTable(List<String> items) {
		return makeTable(items, 16, 4);
	}

	/**
	 * Makes a table-like display of list of items
	 *
	 * @param items        items in the table
	 * @param columnLength length of a column(filled up with whitespace)
	 * @param columns      amount of columns
	 * @return formatted table
	 */
	public static String makeTable(List<String> items, int columnLength, int columns) {
		String ret = "```xl" + Config.EOL;
		int counter = 0;
		for (String item : items) {
			counter++;
			ret += String.format("%-" + columnLength + "s", item);
			if (counter % columns == 0) {
				ret += Config.EOL;
			}
		}
		if (counter % columns != 0) {
			ret += Config.EOL;
		}
		return ret + "```" + Config.EOL;
	}

	/**
	 * @param tableText text
	 * @return formatted table
	 */
	public static String makeTable(String tableText) {
		return "```php" + Config.EOL
				+ tableText + Config.EOL +
				"```" + Config.EOL;
	}

	/**
	 * Turns an array into a string with spaces
	 *
	 * @param list array
	 * @return string spaces between elements
	 */
	public static String concat(String[] list) {
		StringJoiner joiner = new StringJoiner(" ");
		for (String s : list) {
			joiner.add(s);
		}
		return joiner.toString();
	}

	public static String getDurationString(long seconds) {
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		seconds = seconds % 60;
		if (hours > 0) {
			return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
		}
		return twoDigitString(minutes) + ":" + twoDigitString(seconds);
	}

	private static String twoDigitString(long number) {
		if (number == 0) {
			return "00";
		}
		if (number / 10 == 0) {
			return "0" + number;
		}
		return String.valueOf(number);
	}
}
