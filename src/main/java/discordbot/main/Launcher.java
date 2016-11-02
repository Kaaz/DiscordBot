package discordbot.main;

import com.wezinkhof.configuration.ConfigurationBuilder;
import discordbot.core.DbUpdate;
import discordbot.core.ExitCode;
import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OMusic;
import discordbot.db.table.TGuild;
import discordbot.db.table.TMusic;
import discordbot.threads.GrayLogThread;
import discordbot.threads.ServiceHandlerThread;
import discordbot.util.YTUtil;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Launcher {
	public static boolean killAllThreads = false;
	private static GrayLogThread GRAYLOG;
	private static BotContainer botContainer = null;
	private static ProgramVersion version = new ProgramVersion(1);

	/**
	 * log all the things!
	 *
	 * @param message the log message
	 * @param type    the category of the log message
	 * @param subtype the subcategory of a logmessage
	 * @param args    optional extra arguments
	 */
	public static void log(String message, String type, String subtype, Object... args) {
		if (GRAYLOG != null && Config.BOT_GRAYLOG_ACTIVE) {
			GRAYLOG.log(message, type, subtype, args);
		}
	}

	public static ProgramVersion getVersion() {
		return version;
	}

	public static void main(String[] args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("application.cfg")).build();
		WebDb.init();
		Launcher.init();
		if (Config.BOT_ENABLED) {
			try {
				botContainer = new BotContainer((TGuild.getActiveGuildCount()));
				Thread serviceHandler = new ServiceHandlerThread(botContainer);
//				serviceHandler.setDaemon(true);
				serviceHandler.start();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				Launcher.stop(ExitCode.SHITTY_CONFIG);
			}
		} else {
			Logger.fatal("Bot not enabled, enable it in the config. You can do this by setting bot_enabled=true");
			Launcher.stop(ExitCode.SHITTY_CONFIG);
		}
	}

	private static void init() throws IOException, InterruptedException {
		Properties props = new Properties();
		props.load(Launcher.class.getClassLoader().getResourceAsStream("version.properties"));
		Launcher.version = ProgramVersion.fromString(String.valueOf(props.getOrDefault("version", "1")));
		DiscordBot.LOGGER.info("Started with version: " + Launcher.version);
		DbUpdate dbUpdate = new DbUpdate(WebDb.get());
		dbUpdate.updateToCurrent();
		Launcher.GRAYLOG = new GrayLogThread();
		Launcher.GRAYLOG.start();
	}

	/**
	 * Stop the bot!
	 *
	 * @param reason why!?
	 */
	public static void stop(ExitCode reason) {
		if (botContainer != null) {
			for (DiscordBot discordBot : botContainer.getShards()) {
				discordBot.client.shutdown(true);
			}
		}
		DiscordBot.LOGGER.error("Exiting", reason);
		System.exit(reason.getCode());
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