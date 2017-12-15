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

package emily.util;

import com.google.common.base.Strings;
import emily.main.BotConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final static HashSet<String> fuzzyTrue = new HashSet<>(Arrays.asList("yea", "yep", "yes", "true", "ja", "y", "t", "1", "check"));
    private final static HashSet<String> fuzzyFalse = new HashSet<>(Arrays.asList("no", "false", "nope", "nein", "nee", "n", "f", "0"));
    private final static Pattern patternGuildEmote = Pattern.compile("<:.*:(\\d+)>");

    /**
     * check if the sting is a guild emote
     *
     * @param emote string to check
     * @return is it a guild emote?
     */
    public static boolean isGuildEmote(String emote) {
        return patternGuildEmote.matcher(emote).matches();
    }

    /**
     * returns the ID part of a guild emote
     *
     * @param emote the emote to extract from
     * @return id
     */
    public static String getGuildEmoteId(String emote) {
        Matcher matcher = patternGuildEmote.matcher(emote);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            return matcher.group(1);
        }
        if (emote.matches("^\\d+$")) {
            return emote;
        }
        return null;
    }

    public static String makeProgressbar(int max, int current) {
        int parts = 8;
        String bar = "";
        int activeBLock = Math.min(parts - 1, (int) ((float) current / (float) max * (float) parts));
        for (int i = 0; i < parts; i++) {
            if (i == activeBLock) {
                bar += ":large_orange_diamond:";
            } else {
                bar += "▬";
            }
        }
        return bar;
    }

    public static String makeStackedBar(int max, int bar, String barChar) {
        String fill = ":wavy_dash:";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bar; i++) {
            sb.append(barChar);
        }
        for (int i = bar; i < max; i++) {
            sb.append(fill);
        }
        return sb.toString();
    }

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
        return fuzzyTrue.contains(text);
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
        return fuzzyFalse.contains(text);
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

    public static String emoteToNumber(String emote) {
        for (int i = 0; i < numberToEmote.length; i++) {
            if (numberToEmote[i].equals(emote)) {
                return "" + i;
            }
        }
        return "0";
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
        String ret = "```xl" + BotConfig.EOL;
        int counter = 0;
        for (String item : items) {
            counter++;
            ret += String.format("%-" + columnLength + "s", item);
            if (counter % columns == 0) {
                ret += BotConfig.EOL;
            }
        }
        if (counter % columns != 0) {
            ret += BotConfig.EOL;
        }
        return ret + "```" + BotConfig.EOL;
    }

    /**
     * @param tableText text
     * @return formatted controllers
     */
    public static String makeTable(String tableText) {
        return "```" + BotConfig.EOL
                + tableText + BotConfig.EOL +
                "```" + BotConfig.EOL;
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
        sb.append("```").append(BotConfig.EOL);
        String formatLine = "|";
        for (int width : widths) {
            formatLine += " %-" + width + "s |";
        }
        formatLine += BotConfig.EOL;
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
        return ret.append(right).append(BotConfig.EOL).toString();
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
        return joinStrings(strings, startIndex, strings.length);
    }

    /**
     * Joins an array of strings together to 1 starting at position x ending at position y
     *
     * @param strings    the array of strings to join
     * @param startIndex index to start at
     * @param endIndex   index to end at
     * @return joined string
     */
    public static String joinStrings(String[] strings, int startIndex, int endIndex) {
        if (startIndex < strings.length) {
            String ret = strings[startIndex];
            endIndex = Math.min(endIndex, strings.length);
            for (int i = startIndex + 1; i < endIndex; i++) {
                ret += " " + strings[i];
            }
            return ret;
        }
        return "";
    }

    public static int parseInt(String intString, int fallback) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static long parseLong(String longstr, int fallback) {
        try {
            return Long.parseLong(longstr);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
