package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Optional;

/**
 * Successfully logged in to discord
 */
public class TrackFinishedListener extends AbstractEventListener<TrackFinishEvent> {
	public TrackFinishedListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerActivated() {
		return true;
	}

	@Override
	public void handle(TrackFinishEvent event) {
		System.out.println("EVENT::: TRACK FINISHED");
		String botmsg = ":notes:";
		Optional<AudioPlayer.Track> newTrack = event.getNewTrack();
		if (!newTrack.isPresent()) {
			botmsg += ":notes: No more tracks to play, starting randomly :100: :100: :notes: ";
			novaBot.addSongToQueue(getRandomSong(), event.getPlayer().getGuild());
		}
		else{
			botmsg=" next song!";
		}
		IChannel channel = event.getPlayer().getGuild().getChannels().get(0);
		novaBot.sendMessage(channel, botmsg);
		event.getPlayer().isReady();
	}

	private String getRandomSong() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		return fileList[(int) (Math.random() * fileList.length)];
	}
}