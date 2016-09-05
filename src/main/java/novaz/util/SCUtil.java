package novaz.util;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import novaz.main.Config;
import novaz.util.obj.SCFile;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * dealings with soundcloud
 */
public class SCUtil {

	public static boolean isEnabled() {
		return Config.MODULE_MUSIC_ENABLED && new File(Config.MUSIC_DOWNLOAD_SOUNDCLOUD_EXE).exists();
	}

	/**
	 * downloads a youtube video as an mp3
	 *
	 * @param url soundcloud song or playlist
	 * @return success or not
	 */
	public static boolean download(String url) {
		if (!isEnabled()) {
			return false;
		}
		System.out.println("SC:: downloading " + url);
		System.out.println("S:: URL " + url);
		List<String> infoArgs = new LinkedList<>();
		infoArgs.add("java");
		infoArgs.add("-jar");
		infoArgs.add(Config.MUSIC_DOWNLOAD_SOUNDCLOUD_EXE);
		infoArgs.add("--outdirectory");
		infoArgs.add(Config.MUSIC_DIRECTORY + "soundcloud/");
		infoArgs.add("--apitoken");
		infoArgs.add(Config.MUSIC_DOWNLOAD_SOUNDCLOUD_API_TOKEN);
		infoArgs.add(url);
		ProcessBuilder builder = new ProcessBuilder().command(infoArgs);
		builder.redirectErrorStream(true);
		Process process = null;
		try {
			process = builder.start();
			InputStream stdout = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println("SC: " + line);
			}
			process.waitFor();
			process.destroy();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * returns a list of downloaded files yet to process
	 */
	public static List<SCFile> getDownloadedList() {
		File file = new File(Config.MUSIC_DIRECTORY + "soundcloud/");
		File[] files = file.listFiles();
		ArrayList<SCFile> ret = new ArrayList<>();
		if (files == null) {
			return ret;
		}
		for (File f : files) {
			ret.add(getMp3Details(f));
		}
		return ret;
	}

	private static SCFile getMp3Details(File f) {
		SCFile sc = new SCFile();
		try {
			Mp3File mp3file = new Mp3File(f);
			ID3v2 tag3 = mp3file.getId3v2Tag();
			sc.artist = tag3.getArtist();
			sc.title = tag3.getTitle();
			sc.id = "sc_" + f.getName().replace(".mp3", "");
			sc.filename = f.getName();
		} catch (IOException | InvalidDataException | UnsupportedTagException e) {
			e.printStackTrace();
		}
		return sc;
	}
}