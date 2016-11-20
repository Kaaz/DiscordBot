package discordbot.threads;

import discordbot.db.controllers.CBotEvent;
import discordbot.db.model.OBotEvent;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.Launcher;
import discordbot.util.YTUtil;
import net.dv8tion.jda.entities.Message;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Threads for grabbing tracks from youtube
 */
public class YoutubeThread extends Thread {
	private LinkedBlockingQueue<YoutubeTask> queue = new LinkedBlockingQueue<>();
	private final HashSet<String> itemsInProgress = new HashSet<>();
	private volatile boolean threadTerminated = false;
	ExecutorService executor = Executors.newFixedThreadPool(3);

	public YoutubeThread() throws InterruptedException {
		super("yt-to-mp3");
	}

	public synchronized void registerProgress(String youtubeCode) {
		itemsInProgress.add(youtubeCode);

	}

	public synchronized void unRegisterProgress(String youtubeCode) {
		itemsInProgress.remove(youtubeCode);
	}

	public synchronized boolean isInProgress(String youtubeCode) {
		return itemsInProgress.contains(youtubeCode);
	}

	public int getQueueSize() {
		return queue.size();
	}

	public void run() {
		try {
			YoutubeTask task;
			while (!Launcher.isBeingKilled && !threadTerminated) {
				try {
					task = queue.take();
					executor.execute(new YTWorkerWorker(task.getCode(), task));
					sleep(1_000L);
				} catch (InterruptedException e) {
					CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "yt worker broke " + e.getMessage());
				}
			}
		} finally {
			if (!Launcher.isBeingKilled) {
				CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "Youtube-dl thread is dead!");
			}
			threadTerminated = true;
		}
	}

	public void addToQueue(String youtubeCode, String youtubeTitle, Message message, Consumer<Message> callback) {
		if (threadTerminated) {
			CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "Youtube-dl thread is dead!");
			return;
		}
		queue.offer(new YoutubeTask(youtubeCode, youtubeTitle, message, callback));
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

	public static boolean downloadPlayList(String playlist) {
		System.out.println("YT:: downloading " + playlist);
		List<String> infoArgs = new LinkedList<>();
		infoArgs.add(Config.YOUTUBEDL_EXE);
		infoArgs.add("--verbose");
		infoArgs.add("--no-check-certificate");
		infoArgs.add("-x"); //audio only
		infoArgs.add("--ignore-errors");
		infoArgs.add("--prefer-avconv");
		infoArgs.add("--ffmpeg-location");
		infoArgs.add(Config.YOUTUBEDL_BIN);
		infoArgs.add("--audio-format");
		infoArgs.add("mp3");
		infoArgs.add("--socket-timeout");
		infoArgs.add("300");//give up after 600s
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
				System.out.println("STARTED WORKING ON:: " + task.getCode());
				if (isInProgress(task.getCode())) {
					System.out.println("ALREADY IN PROGRESS:: " + task.getCode());
					task.getMessage().updateMessageAsync(Template.get("music_downloading_in_progress", task.getTitle()), null);
					return;
				}
				registerProgress(task.getCode());
				final File fileCheck = new File(YTUtil.getOutputPath(task.getCode()));
				if (!fileCheck.exists()) {
					System.out.println("DOWNLOADING:: " + task.getCode());
					task.getMessage().updateMessageAsync(Template.get("music_downloading_hang_on"), null);
					downloadFromYoutubeAsMp3(task.getCode());
				}
				System.out.println("DONE:: " + task.getCode());
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
