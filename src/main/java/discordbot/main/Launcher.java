package discordbot.main;

import com.wezinkhof.configuration.ConfigurationBuilder;
import discordbot.core.DbUpdate;
import discordbot.core.ExitCode;
import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.controllers.CBotPlayingOn;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CMusic;
import discordbot.db.model.OMusic;
import discordbot.threads.GrayLogThread;
import discordbot.threads.ServiceHandlerThread;
import discordbot.util.YTUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Launcher {
	public volatile static boolean isBeingKilled = false;
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

	public static void logToDiscord(Throwable e, Object... args) {
		if (botContainer != null) {
			botContainer.reportError(e, args);
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
			SimpleLog.addFileLog(SimpleLog.Level.DEBUG, new File("./logs/jda.log"));
			Runtime.getRuntime().addShutdownHook(new Thread(Launcher::shutdownHook));
			try {
				botContainer = new BotContainer((CGuild.getActiveGuildCount()));
				Thread serviceHandler = new ServiceHandlerThread(botContainer);
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
		if (isBeingKilled) {
			return;
		}
		isBeingKilled = true;
		DiscordBot.LOGGER.error("Exiting because: " + reason);
		System.exit(reason.getCode());
	}

	/**
	 * shutdown hook, closing connections
	 */
	private static void shutdownHook() {
		if (botContainer != null) {
			for (DiscordBot discordBot : botContainer.getShards()) {
				for (Guild guild : discordBot.client.getGuilds()) {
					AudioManager audio = guild.getAudioManager();
					if (audio.isConnected()) {
						CBotPlayingOn.insert(guild.getId(), audio.getConnectedChannel().getId());
					}
				}
				discordBot.client.shutdown(true);
			}
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
			OMusic rec = CMusic.findByYoutubeId(videocode);
			rec.youtubeTitle = YTUtil.getTitleFromPage(videocode);
			rec.youtubecode = videocode;
			rec.filename = videocode + ".mp3";
			CMusic.update(rec);
		}
	}
}