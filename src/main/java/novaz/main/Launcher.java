package novaz.main;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import novaz.core.ConfigurationBuilder;
import novaz.db.WebDb;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.handler.MusicPlayerHandler;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.IOException;

import static novaz.handler.MusicPlayerHandler.getTitleFromYoutube;

public class Launcher {

	public static void main(String[] args) throws DiscordException, IOException, InvalidDataException, UnsupportedTagException {
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
			System.out.println(getTitleFromYoutube(videocode));

			OMusic rec = TMusic.findByYoutubeId(videocode);
			rec.title = MusicPlayerHandler.getTitleFromYoutube(videocode);
			rec.youtubecode = videocode;
			rec.filename = videocode + ".mp3";
			TMusic.update(rec);
		}
	}
}