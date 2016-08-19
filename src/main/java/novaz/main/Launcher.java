package novaz.main;

import novaz.core.ConfigurationBuilder;
import novaz.core.Logger;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.util.YTUtil;

import java.io.File;

public class Launcher {

	public static void main(String[] args) throws Exception {

		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		if (Config.BOT_ENABLED.equalsIgnoreCase("true")) {
			NovaBot nb = new NovaBot();
		} else {
			Logger.fatal("Bot not enabled, enable it in the config. You can do this by setting bot_enabled=true");
		}
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
			rec.title = YTUtil.getTitleFromPage(videocode);
			rec.youtubecode = videocode;
			rec.filename = videocode + ".mp3";
			TMusic.update(rec);
		}
	}
}