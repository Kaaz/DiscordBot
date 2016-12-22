package discordbot.threads;

import discordbot.db.controllers.CBotEvent;
import discordbot.db.model.OBotEvent;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.Launcher;
import discordbot.util.YTUtil;
import net.dv8tion.jda.core.entities.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Threads for grabbing tracks from youtube
 */
public class YoutubeThread extends Thread {
	private final HashSet<String> itemsInProgress = new HashSet<>();
	private final AtomicInteger counter = new AtomicInteger(0);
	private final ExecutorService executor;
	private final LinkedBlockingQueue<YoutubeTask> queue = new LinkedBlockingQueue<>();
	private volatile boolean shutdownMode = false;

	public YoutubeThread() throws InterruptedException {
		super("yt-to-mp3");
		executor = Executors.newFixedThreadPool(3);
	}

	/**
	 * downloads a youtube video as an mp3
	 *
	 * @param videocode youtube video id
	 * @return success or not
	 */
	public static boolean downloadFileFromYoutube(String videocode) {
		List<String> infoArgs = new LinkedList<>();
		infoArgs.add(Config.YOUTUBEDL_EXE);
		infoArgs.add("--no-check-certificate");
		infoArgs.add("-x");
		if (Config.YOUTUBEDL_DEBUG_PROCESS) {
			infoArgs.add("-v");
		}
		infoArgs.add("--audio-format");
		infoArgs.add("opus");
		infoArgs.add("--audio-quality");
		infoArgs.add("0");
		infoArgs.add("--prefer-ffmpeg");
		infoArgs.add("--hls-prefer-ffmpeg");
		infoArgs.add("--max-filesize");
		infoArgs.add("64m");
		if (Config.MUSIC_USE_CACHE_DIR) {
			infoArgs.add("--exec");
			infoArgs.add("mv {} " + Config.MUSIC_DIRECTORY);
			infoArgs.add("--output");
			infoArgs.add(Config.MUSIC_CACHE_DIR + videocode + ".%(ext)s");
		} else {
			infoArgs.add("--output");
			infoArgs.add(Config.MUSIC_DIRECTORY + videocode + ".%(ext)s");
		}
		infoArgs.add("https://www.youtube.com/watch?v=" + videocode);
		ProcessBuilder builder = new ProcessBuilder().command(infoArgs);
		builder.redirectErrorStream(true);
		try {
			Process process = builder.start();
			if (Config.YOUTUBEDL_DEBUG_PROCESS) {
				StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
				StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
				errorGobbler.start();
				outputGobbler.start();
			}
			process.waitFor(5, TimeUnit.MINUTES);
			process.destroy();
		} catch (IOException | InterruptedException e) {
			Launcher.logToDiscord(e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public synchronized void registerProgress(String youtubeCode) {
		itemsInProgress.add(youtubeCode);
	}

	public synchronized void unRegisterProgress(String youtubeCode) {
		itemsInProgress.remove(youtubeCode);
		counter.decrementAndGet();
	}

	public void shutown() {
		shutdownMode = true;
	}

	public synchronized boolean isInProgress(String youtubeCode) {
		return itemsInProgress.contains(youtubeCode);
	}

	public int getQueueSize() {
		return counter.get();
	}

	public void run() {
		try {
			YoutubeTask task;

			while (!shutdownMode && !Launcher.isBeingKilled) {
				try {
					task = queue.take();
					executor.execute(new YTWorkerWorker(task.getCode(), task));
					counter.incrementAndGet();
				} catch (InterruptedException e) {
					Launcher.logToDiscord(e);
					CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "yt worker broke " + e.getMessage());
				}
			}
		} finally {
			if (!shutdownMode && !Launcher.isBeingKilled) {
				CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "Youtube-dl thread is dead!");
			}
			shutdownMode = true;
		}
	}

	public void addToQueue(String youtubeCode, String youtubeTitle, Message message, Consumer<Message> callback) {
		if (shutdownMode) {
			CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "Youtube-dl thread is dead!");
			return;
		}
		queue.offer(new YoutubeTask(youtubeCode, youtubeTitle, message, callback));
	}

	private static class StreamGobbler extends Thread {
		private InputStream is;

		public StreamGobbler(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException ex) {
				// ex.printStackTrace();
			}
		}
	}

	private class YoutubeTask {
		private final String code;
		private final String title;
		private final Message message;
		private final Consumer<Message> callback;

		public YoutubeTask(String code, String title, Message message, Consumer<Message> callback) {
			this.code = code;
			this.message = message;
			this.callback = callback;
			this.title = title;

		}

		public Consumer<Message> getCallback() {
			return callback;
		}

		public Message getMessage() {
			return message;
		}

		public String getCode() {
			return code;
		}

		public String getTitle() {
			return title;
		}
	}

	class YTWorkerWorker extends Thread {
		private final YoutubeTask task;

		YTWorkerWorker(String identifier, YoutubeTask task) {
			super("yt-to-mp3 worker #" + identifier);
			this.task = task;
		}

		public void run() {
			try {
				if (shutdownMode) {
					return;
				}
				if (isInProgress(task.getCode())) {
					task.getMessage().editMessage(Template.get("music_downloading_in_progress", task.getTitle())).queue();
					return;
				}
				registerProgress(task.getCode());
				final File fileCheck = new File(YTUtil.getOutputPath(task.getCode()));
				if (!fileCheck.exists()) {
					task.getMessage().editMessage(Template.get("music_downloading_hang_on")).queue();
					downloadFileFromYoutube(task.getCode());
				}
				if (task.getCallback() != null) {
					task.getCallback().accept(task.getMessage());
				}
			} catch (Exception e) {
				Launcher.logToDiscord(e, "yt-code", task.getCode());
				CBotEvent.insert(OBotEvent.Level.WARN, ":octagonal_sign:", ":musical_note:", " ytcode: " + task.getCode() + "  " + e.getMessage());
			} finally {
				unRegisterProgress(task.getCode());
			}
		}
	}
}