package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * !play
 * plays a file/url
 */
public class Play extends AbstractCommand {
	Pattern yturl = Pattern.compile("^.*((youtu.be/)|(v/)|(/u/\\w/)|(embed/)|(watch\\?))\\\\??v?=?([^#\\\\&\\?]*).*");

	public Play(NovaBot b) {
		super(b);
		setCmd("play");
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		String filename = "test";
		if (args.length > 0) {
			String videocode = extractvideocodefromyoutubeurl(args[0]);
			File filecheck = new File(Config.MUSIC_DIRECTORY + videocode + ".mp3");
			if (!filecheck.exists()) {
				downloadfromYoutube(videocode);
			}
			if (filecheck.exists()) {
				filename = videocode;
				bot.playAudioFromFile(filename + ".mp3", channel.getGuild());
				return TextHandler.get("music_added_to_queue");
			}
		}
		return TextHandler.get("music_not_added_to_queue");
	}

	private String extractvideocodefromyoutubeurl(String url) {
		Matcher matcher = yturl.matcher(url);
		if (matcher.find()) {
			return matcher.group(7);
		}
		return url;
	}

	private boolean downloadfromYoutube(String videocode) {
		System.out.println("YT:: downloading " + videocode);
		System.out.println("YT:: https://www.youtube.com/watch?v=" + videocode);
		List<String> infoArgs = new LinkedList<>();
		infoArgs.add(Config.YOUTUBEDL_EXE);
		infoArgs.add("--verbose");
		infoArgs.add("--no-check-certificate");
		infoArgs.add("-x"); //audio only
		infoArgs.add("--prefer-avconv");
		infoArgs.add("--ffmpeg-location");
		infoArgs.add(Config.YOUTUBEDL_BIN);
		infoArgs.add("--audio-format");
		infoArgs.add("mp3");
		infoArgs.add("--output");
		infoArgs.add(Config.MUSIC_DIRECTORY + videocode + ".%(ext)s");
		infoArgs.add("https://www.youtube.com/watch?v=" + videocode);
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
}