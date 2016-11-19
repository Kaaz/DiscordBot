package discordbot.threads;

import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.Launcher;
import net.dv8tion.jda.entities.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Thread for grabbing tracks from youtube
 */
public class YoutubeThread extends Thread {
	private LinkedBlockingQueue<YoutubeTask> queue = new LinkedBlockingQueue<>();
	private volatile boolean threadTerminated = false;

	public YoutubeThread() throws InterruptedException {
		super("yt-to-mp3");
	}


	public int getQueueSize() {
		return queue.size();
	}

	public void run() {
		try {
			YoutubeTask task;
			while (!Launcher.isBeingKilled && !threadTerminated) {
				task = queue.take();
				task.getMessage().updateMessageAsync(Template.get("music_downloading_hang_on"), null);
				downloadFromYoutubeAsMp3(task.getCode());
				if (task.getCallback() != null) {
					task.getCallback().accept(task.getMessage());
				}
			}
		} catch (InterruptedException iex) {
		} finally {
			threadTerminated = true;
		}
	}

	public void addToQueue(String youtubeCode, Message message) {
		addToQueue(youtubeCode, message, null);
	}

	public void addToQueue(String youtubeCode, Message message, Consumer<Message> callback) {
		if (threadTerminated) return;
		try {
			queue.put(new YoutubeTask(youtubeCode, message, callback));
		} catch (InterruptedException iex) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Unexpected interruption");
		}
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
			process.waitFor();
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

	private class YoutubeTask {
		private final String code;
		private final Message message;
		private final Consumer<Message> callback;

		public YoutubeTask(String code, Message message, Consumer<Message> callback) {

			this.code = code;
			this.message = message;
			this.callback = callback;
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
	}
}
