package novaz.util;

import novaz.main.Config;

import java.util.ArrayList;
import java.util.StringJoiner;

public class Misc {

	/**
	 * @param items items in the table
	 * @return formatted table
	 */
	public static String makeTable(ArrayList<String> items) {
		return makeTable(items, 16, 4);
	}

	/**
	 * Makes a table-like display of list of items
	 *
	 * @param items    items in the table
	 * @param colSize  length of a column(filled up with whitespace)
	 * @param colCount amount of columns
	 * @return formatted table
	 */
	public static String makeTable(ArrayList<String> items, int colSize, int colCount) {
		String ret = "```ini" + Config.EOL;
		int counter = 0;
		for (String item : items) {
			counter++;
			ret += String.format("%-" + colSize + "s", item);
			if (counter % colCount == 0) {
				ret += Config.EOL;
			}
		}
		if (counter % colCount != 0) {
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
}
