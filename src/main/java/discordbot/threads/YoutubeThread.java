package discordbot.threads;

import discordbot.db.controllers.CBotEvent;
import discordbot.db.model.OBotEvent;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.Launcher;
import discordbot.util.YTUtil;
import net.dv8tion.jda.entities.Message;

import java.io.File;
import java.io.IOException;
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
	ExecutorService executor = Executors.newFixedThreadPool(3);
	private LinkedBlockingQueue<YoutubeTask> queue = new LinkedBlockingQueue<>();
	private volatile boolean shutdownMode = false;
	private final AtomicInteger counter = new AtomicInteger();

	public YoutubeThread() throws InterruptedException {
		super("yt-to-mp3");
	}

	/**
	 * downloads a youtube video as an mp3
	 *
	 * @param videocode youtube video id
	 * @return success or not
	 */
	public static boolean downloadFromYoutubeAsMp3(String videocode) {
		List<String> infoArgs = new LinkedList<>();
		infoArgs.add(Config.YOUTUBEDL_EXE);
		infoArgs.add("--verbose");
		infoArgs.add("--no-check-certificate");
		infoArgs.add("-x"); //audio only
		infoArgs.add("--ffmpeg-location");
		infoArgs.add(Config.YOUTUBEDL_BIN);
		infoArgs.add("--audio-format");
		infoArgs.add("mp3");
		infoArgs.add("--max-filesize");
		infoArgs.add("64m");
		infoArgs.add("--output");
		infoArgs.add(Config.MUSIC_DIRECTORY + "/" + videocode + ".%(ext)s");
		infoArgs.add("https://www.youtube.com/watch?v=" + videocode);
		ProcessBuilder builder = new ProcessBuilder().command(infoArgs);
		builder.redirectErrorStream(true);

		Process process = null;
		try {
			process = builder.start();
			process.waitFor(10, TimeUnit.MINUTES);
			process.destroy();
		} catch (IOException | InterruptedException e) {
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
			while (!Launcher.isBeingKilled) {
				try {
					task = queue.take();
					executor.execute(new YTWorkerWorker(task.getCode(), task));
					counter.incrementAndGet();
					sleep(1_000L);
				} catch (InterruptedException e) {
					CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "yt worker broke " + e.getMessage());
				}
			}
		} finally {
			if (!Launcher.isBeingKilled) {
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

	private class YoutubeTask {
		private final String code;
		private final String title;
		private final Message message;
		private final Consumer<Message> callback;

		public YoutubeTask(String code, Message message, Consumer<Message> callback) {

			this.code = code;
			this.message = message;
			this.callback = callback;
			this.title = code;
		}

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
				if (isInProgress(task.getCode())) {
					task.getMessage().updateMessageAsync(Template.get("music_downloading_in_progress", task.getTitle()), null);
					return;
				}
				registerProgress(task.getCode());
				final File fileCheck = new File(YTUtil.getOutputPath(task.getCode()));
				if (!fileCheck.exists()) {
					task.getMessage().updateMessageAsync(Template.get("music_downloading_hang_on"), null);
					downloadFromYoutubeAsMp3(task.getCode());
				}
				if (task.getCallback() != null) {
					task.getCallback().accept(task.getMessage());
				}
			} catch (Exception e) {
				CBotEvent.insert(OBotEvent.Level.WARN, ":octagonal_sign:", ":musical_note:", " ytcode: " + task.getCode() + "  " + e.getMessage());
			} finally {
				unRegisterProgress(task.getCode());
			}
		}
	}
}