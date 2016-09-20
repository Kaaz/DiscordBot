package discordbot.main;

import com.wezinkhof.configuration.ConfigurationBuilder;
import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.threads.ServiceHandlerThread;
import discordbot.util.YTUtil;

import java.io.File;

public class Launcher {
	public static boolean killAllThreads = false;

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		WebDb.init();
		if (Config.BOT_ENABLED) {
			NovaBot nb = new NovaBot();
			Thread serviceHandler = new ServiceHandlerThread(nb);
			serviceHandler.setDaemon(true);
			serviceHandler.start();
		} else {
			Logger.fatal("Bot not enabled, enable it in the config. You can do this by setting bot_enabled=true");
		}
	}

	/**
	 * helper function, retrieves youtubeTitle for mp3 files which contain youtube videocode as filename
	 */
	public static void fixExistingYoutubeFiles() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		for (String file : fileList) {
			System.out.println(file);
			String videocode = file.replace(".mp3", "");
			OMusic rec = TMusic.findByYoutubeId(videocode);
			rec.youtubeTitle = YTUtil.getTitleFromPage(videocode);
			rec.youtubecode = videocode;
			rec.filename = videocode + ".mp3";
			TMusic.update(rec);
		}
	}

}