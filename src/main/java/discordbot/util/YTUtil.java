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

package discordbot.util;

import discordbot.db.controllers.CMusic;
import discordbot.db.model.OMusic;
import discordbot.main.Config;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;
import sun.misc.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTUtil {
	public final static Pattern yturl = Pattern.compile("^(?:https?:\\/\\/)?(?:(?:www\\.)?)?(?:youtube\\.com|youtu\\.be)\\/.*?(?:embed|e|v|watch.*?v=)?\\/?([-a-z0-9]{10,})?(?:&?index=\\d+)?(?>(?:playlist\\?|&)?list=([^#\\\\&\\?]{12,}))?", Pattern.CASE_INSENSITIVE);
	private final static Pattern youtubeCode = Pattern.compile("^[A-Za-z0-9_-]{11}$");

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
			if (matcher.group(1) != null) {
				return matcher.group(1);
			}
		}
		return url;
	}

	/**
	 * Extracts the playlistcode from a yt url
	 *
	 * @param url the url
	 * @return playlistcode || null if not found
	 */
	public static String getPlayListCode(String url) {
		Matcher matcher = yturl.matcher(url);
		if (matcher.find()) {
			if (matcher.groupCount() == 2) {
				return matcher.group(2);
			}
		}
		return null;
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
		return Config.MUSIC_DIRECTORY + videoCode + ".opus";
	}

	public static boolean getTrackDuration(OMusic record) {
		if (record.fileExists == 0 || record.duration > 0) {
			return false;
		}
		Process ffprobeProcess = null;
		try {
			ffprobeProcess = new ProcessBuilder().command(Arrays.asList(
					"ffprobe",
					"-show_format",
					"-print_format", "json",
					"-loglevel", "0",
					"-i", record.filename
			)).start();
			InputStream ffprobeStream = ffprobeProcess.getInputStream();
			byte[] infoData = IOUtils.readFully(ffprobeStream, -1, false);
			ffprobeProcess.waitFor(30, TimeUnit.SECONDS);
			if (infoData != null && infoData.length > 0) {
				JSONObject json = new JSONObject(new String(infoData)).getJSONObject("format");
				int duration = (int) json.optDouble("duration", 0);
				if (duration != 0) {
					record.duration = duration;
					CMusic.update(record);
					return true;
				}
			}
		} catch (IOException | InterruptedException ignored) {
		} finally {
			if (ffprobeProcess != null) {
				ffprobeProcess.destroyForcibly();
			}
		}
		return false;
	}
}
