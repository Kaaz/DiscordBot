package novaz.handler;

import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.main.Config;
import novaz.main.NovaBot;
import org.apache.commons.lang3.StringEscapeUtils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MusicPlayerHandler {
	private final static Map<IGuild, MusicPlayerHandler> playerInstances = new ConcurrentHashMap<>();
	private final IGuild guild;
	private final NovaBot bot;
	private IMessage activeMsg;

	public static MusicPlayerHandler getAudioPlayerForGuild(IGuild guild, NovaBot bot) {
		if (playerInstances.containsKey(guild)) {
			return playerInstances.get(guild);
		} else {
			return new MusicPlayerHandler(guild, bot);
		}
	}

	public void skipSong() {
		clearMessage();
		AudioPlayer ap = AudioPlayer.getAudioPlayerForGuild(guild);
		ap.skip();
		if (ap.getPlaylistSize() == 0) {
			playRandomSong();
		}
	}

	private MusicPlayerHandler(IGuild guild, NovaBot bot) {
		this.guild = guild;
		this.bot = bot;
		playerInstances.put(guild, this);
	}

	private String getRandomSong() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		return fileList[(int) (Math.random() * (double) fileList.length)];
	}

	public void onTrackEnded(AudioPlayer.Track oldTrack, Optional<AudioPlayer.Track> nextTrack) {
		clearMessage();
		if (!nextTrack.isPresent()) {
			playRandomSong();
		}
	}

	public void onTrackStarted(AudioPlayer.Track track) {
		clearMessage();
		Map<String, Object> metadata = track.getMetadata();
		String msg = "Now playing unknown file :(";
		if (metadata.containsKey("file")) {
			if (metadata.get("file") instanceof File) {
				File f = (File) metadata.get("file");
				OMusic music = TMusic.findByFileName(f.getName());
				if (music.title.isEmpty()) {
					msg = "plz send help:: " + f.getName();
				} else {
					msg = "Now playing " + music.title;
				}
			}
		}
		activeMsg = bot.sendMessage(guild.getChannels().get(0), msg);
	}

	private void clearMessage() {
		if (activeMsg != null) {
			try {
				activeMsg.delete();
				activeMsg = null;
			} catch (MissingPermissionsException | RateLimitException | DiscordException ignored) {
			}
		}
	}

	public void playRandomSong() {
		String randomSong = getRandomSong();
		try {
			AudioPlayer.getAudioPlayerForGuild(guild).queue(new File(Config.MUSIC_DIRECTORY + randomSong));
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	public static String getTitleFromYoutube(String videocode) {
		String ret = "";
		try {
			URL loginurl = new URL("https://www.youtube.com/watch?v=" + videocode);
			URLConnection yc = loginurl.openConnection();
			yc.setConnectTimeout(10 * 1000);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String input = "";
			String inputLine = "";
			while ((inputLine = in.readLine()) != null)
				input += inputLine;
			in.close();
			int start = input.indexOf("<title>");
			int end = input.indexOf("</title>");
			ret = input.substring(start + 7, end - 10);
		} catch (Exception e) {
			System.out.println(e);
		}
		return StringEscapeUtils.unescapeHtml4(ret);
	}

	public void stopMusic() {
		AudioPlayer.getAudioPlayerForGuild(guild).clear();
	}
}
