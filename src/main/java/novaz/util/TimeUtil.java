package novaz.util;

public class TimeUtil {
	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

	/**
	 * @param time timestamp in seconds
	 * @return string of x min ago
	 */
	public static String getTimeAgo(long time) {
		return getTimeAgo(time, true);
	}

	/**
	 * @param time      timestamp in seconds
	 * @param shortText short or long text
	 * @return string of x min ago
	 */
	public static String getTimeAgo(long time, boolean shortText) {
		System.out.println(time);
		time = time * 1000;
		long now = System.currentTimeMillis();
		if (time > now || time <= 0) {
			return "???";
		}
		long diff = now - time;
		if (diff < MINUTE_MILLIS) {
			return "now";
		} else if (diff < 2 * MINUTE_MILLIS) {
			return shortText ? "~1m" : "About a minute ago";
		} else if (diff < 50 * MINUTE_MILLIS) {
			return diff / MINUTE_MILLIS + (shortText ? "m" : " Minutes ago");
		} else if (diff < 90 * MINUTE_MILLIS) {
			return shortText ? "~1h" : "About an hour ago";
		} else if (diff < 24 * HOUR_MILLIS) {
			return diff / HOUR_MILLIS + (shortText ? "h" : " Hours ago");
		} else if (diff < 48 * HOUR_MILLIS) {
			return "yesterday";
		} else if (diff < 14 * DAY_MILLIS || !shortText) {
			return diff / DAY_MILLIS + (shortText ? "d" : " Days ago");
		}
		return ">2w";
	}
}
