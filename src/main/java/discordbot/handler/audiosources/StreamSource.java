package discordbot.handler.audiosources;

import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.AudioStream;
import net.dv8tion.jda.player.source.LocalStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

public class StreamSource implements AudioSource {
	private String url;

	public StreamSource(String url) {
		this.url = url;
	}

	@Override
	public String getSource() {
		return null;
	}

	@Override
	public AudioInfo getInfo() {
		return null;
	}

	@Override
	public AudioStream asStream() {
		return new LocalStream(
				Arrays.asList(
						"ffmpeg",
						"-i", url,
						"-f", "s16be",
						"-ac", "2",
						"-ar", "48000",
						"-map", "a",
						"-"
				));
	}

	@Override
	public File asFile(String path, boolean deleteOnExists) throws FileAlreadyExistsException, FileNotFoundException {
		return null;
	}
}
