package novaz.main;

import novaz.core.ConfigurationBuilder;
import novaz.db.WebDb;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.handler.MusicPlayerHandler;
import sx.blah.discord.util.DiscordException;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Launcher {

	public static void main(String[] args) throws DiscordException, IOException {
		WebDb.init();
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		NovaBot nb = new NovaBot();
	}

	/**
	 * helper function, retrieves title for mp3 files which contain youtube videocode as filename
	 */
	public static void fixExistingYoutubeFiles() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		for (String file : fileList) {
			String videocode = file.replace(".mp3", "");
			OMusic rec = TMusic.findByYoutubeId(videocode);
			rec.title = MusicPlayerHandler.getTitleFromYoutube(videocode);
			rec.youtubecode = videocode;
			rec.filename = videocode + ".mp3";
			TMusic.update(rec);
		}
	}

	public static boolean downloadfromYoutube(String playlist) {
		System.out.println("YT:: downloading " + playlist);
		List<String> infoArgs = new LinkedList<>();
		infoArgs.add(Config.YOUTUBEDL_EXE);
		infoArgs.add("--verbose");
		infoArgs.add("--no-check-certificate");
		infoArgs.add("-x"); //audio only
		infoArgs.add("--ignore-errors"); //audio only
		infoArgs.add("--prefer-avconv");
		infoArgs.add("--ffmpeg-location");
		infoArgs.add(Config.YOUTUBEDL_BIN);
		infoArgs.add("--audio-format");
		infoArgs.add("mp3");
		infoArgs.add("--output");
		infoArgs.add(Config.MUSIC_DIRECTORY + "tmp/%(id)s.%(ext)s");
		infoArgs.add(playlist);
		ProcessBuilder builder = new ProcessBuilder().command(infoArgs);
		builder.redirectErrorStream(true);
		Process process = null;
		try {
			process = builder.start();
			InputStream stdout = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println("YT: " + line);
			}
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}