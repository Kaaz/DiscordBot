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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeUtil {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final SimpleDateFormat timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final Map<Character, Long> TIME_SYMBOLS = new HashMap<>();

    static {
        TIME_SYMBOLS.put('w', 604800000L);
        TIME_SYMBOLS.put('d', 86400000L);
        TIME_SYMBOLS.put('h', 3600000L);
        TIME_SYMBOLS.put('m', 60000L);
        TIME_SYMBOLS.put('s', 1000L);
    }

    public static String getTimestampFormat(long time) {
        try {
            return timestamp.format(new Date(time));
        } catch (Exception e) {
            return "cant figure out (" + time + ")";
        }
    }

    public static String formatYMD(Date date) {
        return ymdFormat.format(date);
    }

    /**
     * @param time timestamp in seconds
     * @return string of x min ago
     */
    public static String getRelativeTime(long time) {
        return getRelativeTime(time, true);
    }

    /**
     * @param time      timestamp in seconds
     * @param shortText short or long text
     * @return string of x min ago
     */
    public static String getRelativeTime(long time, boolean shortText) {
        return getRelativeTime(time, shortText, true);
    }

    /**
     * @param time           timestamp in seconds
     * @param shortText      short or long texts
     * @param showChronology show when it was (ago or from now)
     * @return x m[inutes [from now]]
     */
    public static String getRelativeTime(long time, boolean shortText, boolean showChronology) {
        long usedTime = time * 1000L;
        boolean future = false;
        String chronology = "ago";
        long now = System.currentTimeMillis();
        if (usedTime <= 0) {
            return "???";
        }
        long diff;
        if (usedTime > now) {
            diff = usedTime - now;
            chronology = "from now";
            future = true;
        } else {
            diff = now - usedTime;
        }
        if (!showChronology) {
            chronology = "";
        }
        if (diff < MINUTE_MILLIS) {
            return (diff / SECOND_MILLIS) + (shortText ? "s" : " seconds " + chronology);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return shortText ? "~1m" : "about a minute " + chronology;
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + (shortText ? "m" : " minutes " + chronology);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return shortText ? "~1h" : "about an hour " + chronology;
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + (shortText ? "h" : " hour" + (diff / HOUR_MILLIS == 1 ? "" : "s") + " " + chronology);
        } else if (diff < 48 * HOUR_MILLIS) {
            return shortText ? "~1d" : future ? showChronology ? "tomorrow" : "about a day" : "yesterday";
        } else if (diff < 14 * DAY_MILLIS || !shortText) {
            return diff / DAY_MILLIS + (shortText ? "d" : " day" + (diff == 1 ? "" : "s") + " " + chronology);
        }
        return ">2w";
    }

    /**
     * Takes the value of the string as represented by trailing
     * w, d, h, m, or s characters and gets a millisecond value
     * from them.  Any values with no label are added as millis.
     *
     * @param s the string to be parsed
     * @return the value of the string in milliseconds
     * or null if it can not be parsed
     */
    public static long toMillis(String s) {
        s = s.toLowerCase();
        long val = 0;
        StringBuilder working = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                working.append(s.charAt(i));
            } else if (TIME_SYMBOLS.containsKey(s.charAt(i))) {
                if (working.length() > 0) {
                    val += Misc.parseInt(working.toString(), 0) * TIME_SYMBOLS.get(s.charAt(i));
                    working = new StringBuilder();
                }
            }
        }
        if (working.length() != 0) {
            val += Misc.parseInt(working.toString(), 0);
        }
        return val;
    }
}
