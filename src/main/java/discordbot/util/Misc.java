package discordbot.util;

import com.google.common.base.Strings;
import discordbot.main.Config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class Misc {
	private static final String[] numberToEmote = {
			"\u0030\u20E3",
			"\u0031\u20E3",
			"\u0032\u20E3",
			"\u0033\u20E3",
			"\u0034\u20E3",
			"\u0035\u20E3",
			"\u0036\u20E3",
			"\u0037\u20E3",
			"\u0038\u20E3",
			"\u0039\u20E3",
			"\uD83D\uDD1F"
	};

	/**
	 * whether a string can fuzzily considered true
	 *
	 * @param text the string
	 * @return true if it can be considered true
	 */
	public static boolean isFuzzyTrue(String text) {
		if (text == null) {
			return false;
		}
		switch (text.toLowerCase()) {
			case "yea":
			case "yes":
			case "true":
			case "y":
			case "t":
			case "1":
				return true;
			default:
				return false;
		}

	}

	/**
	 * whether a string can fuzzily considered true
	 *
	 * @param text the string to check
	 * @return true if it can be considered false
	 */

	public static boolean isFuzzyFalse(String text) {
		if (text == null) {
			return false;
		}
		switch (text.toLowerCase()) {
			case "no":
			case "false":
			case "nope":
			case "n":
			case "f":
			case "0":
				return true;
			default:
				return false;
		}
	}

	/**
	 * searches a map by value and returns the key if found otherwise null
	 *
	 * @param map   the map to search in
	 * @param value the value to search for
	 * @param <T>   map key type
	 * @param <E>   map value type
	 * @return matched key of the map or null
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Map.Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Converts a numer to an emoji
	 *
	 * @param number number <= 10
	 * @return emoji for that number or :x: if not found
	 */
	public static String numberToEmote(int number) {
		if (number >= 0 && number < numberToEmote.length) {
			return numberToEmote[number];
		}
		return ":x:";
	}

	/**
	 * @param items items in the controllers
	 * @return formatted controllers
	 */
	public static String makeTable(List<String> items) {
		return makeTable(items, 16, 4);
	}

	/**
	 * Makes a controllers-like display of list of items
	 *
	 * @param items        items in the controllers
	 * @param columnLength length of a column(filled up with whitespace)
	 * @param columns      amount of columns
	 * @return formatted controllers
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
	 * @return formatted controllers
	 */
	public static String makeTable(String tableText) {
		return "```" + Config.EOL
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

	/**
	 * returns a formatted string from a time in secnods
	 *
	 * @param seconds input in seconds
	 * @return string hh:mm:ss
	 */
	public static String getDurationString(long seconds) {
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long secs = seconds % 60;
		if (hours > 0) {
			return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(secs);
		}
		return twoDigitString(minutes) + ":" + twoDigitString(secs);
	}

	/**
	 * @param headers array containing the headers
	 * @param table   array[n size] of array's[header size], containing the rows of the controllers
	 * @param footer
	 * @return a formatted controllers
	 */
	public static String makeAsciiTable(List<String> headers, List<List<String>> table, List<String> footer) {
		StringBuilder sb = new StringBuilder();
		int padding = 1;
		int[] widths = new int[headers.size()];
		for (int i = 0; i < widths.length; i++) {
			widths[i] = 0;
		}
		for (int i = 0; i < headers.size(); i++) {
			if (headers.get(i).length() > widths[i]) {
				widths[i] = headers.get(i).length();
				if (footer != null) {
					widths[i] = Math.max(widths[i], footer.get(i).length());
				}
			}
		}
		for (List<String> row : table) {
			for (int i = 0; i < row.size(); i++) {
				String cell = row.get(i);
				if (cell.length() > widths[i]) {
					widths[i] = cell.length();
				}
			}
		}
		sb.append("```").append(Config.EOL);
		String formatLine = "|";
		for (int width : widths) {
			formatLine += " %-" + width + "s |";
		}
		formatLine += Config.EOL;
		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
		sb.append(String.format(formatLine, headers.toArray()));
		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
		for (List<String> row : table) {
			sb.append(String.format(formatLine, row.toArray()));
		}
		if (footer != null) {
			sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
			sb.append(String.format(formatLine, footer.toArray()));
		}
		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
		sb.append("```");
		return sb.toString();
	}

	/**
	 * helper function for makeAsciiTable
	 *
	 * @param left    character on the left
	 * @param middle  character in the middle
	 * @param right   character on the right
	 * @param padding controllers cell padding
	 * @param sizes   width of each cell
	 * @return a filler row for the controllers
	 */
	private static String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
		boolean first = true;
		StringBuilder ret = new StringBuilder();
		for (int size : sizes) {
			if (first) {
				first = false;
				ret.append(left).append(Strings.repeat("-", size + padding * 2));
			} else {
				ret.append(middle).append(Strings.repeat("-", size + padding * 2));
			}
		}
		return ret.append(right).append(Config.EOL).toString();
	}

	/**
	 * ensures that the string is at least 2 digits
	 *
	 * @param number the number to format
	 * @return formatted string
	 */
	private static String twoDigitString(long number) {
		if (number == 0) {
			return "00";
		}
		if (number / 10 == 0) {
			return "0" + number;
		}
		return String.valueOf(number);
	}

	/**
	 * Sorts a map by value descending
	 *
	 * @param map the map to sort
	 * @param <K> key
	 * @param <V> a sortable value
	 * @return the same map but sorted descending
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, (o1, o2) -> -(o1.getValue()).compareTo(o2.getValue()));

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Joins an array of strings together to 1 starting at position x
	 *
	 * @param strings    the strings to join
	 * @param startIndex the index to start at
	 * @return a joined string
	 */
	public static String joinStrings(String[] strings, int startIndex) {
		String ret = "";
		if (startIndex <= strings.length) {
			ret = strings[startIndex];
			for (int i = startIndex + 1; i < strings.length; i++) {
				ret += " " + strings[i];
			}
		}
		return ret;
	}

	public static int parseInt(String intString, int fallback) {
		try {
			return Integer.parseInt(intString);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
