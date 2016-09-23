package discordbot.main;

import com.wezinkhof.configuration.ConfigurationBuilder;
import discordbot.core.ExitCode;
import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.threads.ServiceHandlerThread;
import discordbot.util.UpdateUtil;
import discordbot.util.YTUtil;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.util.Properties;

public class Launcher {
	public static boolean killAllThreads = false;
	private static ProgramVersion version = new ProgramVersion(1);

	public static ProgramVersion getVersion() {
		return version;
	}

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		WebDb.init();
		Properties props = new Properties();
		props.load(Launcher.class.getClassLoader().getResourceAsStream("version.properties"));
		Launcher.version = ProgramVersion.fromString(String.valueOf(props.getOrDefault("version", "1")));
		DiscordBot.LOGGER.info("Started with version: " + Launcher.version);
		if (Config.BOT_ENABLED) {
			DiscordBot nb = null;
			try {
				nb = new DiscordBot();
			} catch (DiscordException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(ExitCode.SHITTY_CONFIG.getCode());
			}
			Thread serviceHandler = new ServiceHandlerThread(nb);
			serviceHandler.setDaemon(true);
			serviceHandler.start();
		} else {
			System.exit(ExitCode.SHITTY_CONFIG.getCode());
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