package discordbot.util;

import discordbot.main.Config;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTUtil {
	private final static Pattern youtubeCode = Pattern.compile("^[A-Za-z0-9_-]{11}$");
	private final static Pattern yturl = Pattern.compile("^.*((youtu.be/)|(v/)|(/u/\\w/)|(embed/)|(watch\\?))\\\\??v?=?([^#\\\\&\\?]*).*");

	/**
	 * checks if it could be a youtube videocode
	 *
	 * @param videocode code to check
	 * @return could be a code
	 */
	public static boolean isValidYoutubeCode(String videocode) {
		return youtubeCode.matcher(videocode).matches();
	}

	/**
	 * Extracts the videocode from an url
	 *
	 * @param url youtube link
	 * @return videocode
	 */
	public static String extractCodeFromUrl(String url) {
		Matcher matcher = yturl.matcher(url);
		if (matcher.find()) {
			return matcher.group(7);
		}
		return url;
	}

	/**
	 * @param videocode youtubecode
	 * @return whats in the <title> tag on a youtube page
	 */
	public static String getTitleFromPage(String videocode) {
		String ret = "";
		try {
			URL loginurl = new URL("https://www.youtube.com/watch?v=" + videocode);
			URLConnection yc = loginurl.openConnection();
			yc.setConnectTimeout(10 * 1000);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String input = "";
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				input += inputLine;
			in.close();
			int start = input.indexOf("<title>");
			int end = input.indexOf("</title>");
			ret = input.substring(start + 7, end - 10);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return StringEscapeUtils.unescapeHtml4(ret);
	}


	public static String getOutputPath(String videoCode) {
		return Config.MUSIC_DIRECTORY + videoCode + ".mp3";
	}
}
