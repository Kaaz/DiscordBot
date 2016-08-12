package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackSkipEvent;

import java.io.File;
import java.util.Optional;

/**
 * Track finished event
 */
public class TrackSkipListener extends AbstractEventListener<TrackSkipEvent> {
	public TrackSkipListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerActivated() {
		return true;
	}

	@Override
	public void handle(TrackSkipEvent event) {
		System.out.println("EVENT::: TRACK END");
//		String botmsg = ":notes:";
//		Optional<AudioPlayer.Track> newTrack = event.getNewTrack();
//		if (!newTrack.isPresent()) {
//			botmsg += ":notes: No more tracks to play, starting randomly :100: :100: :notes: ";
		if (event.getPlayer().getPlaylistSize() == 0) {
			novaBot.addSongToQueue(getRandomSong(), event.getPlayer().getGuild());
		}
//		} else {
//			botmsg = " next song!";
//		}

		IChannel channel = event.getPlayer().getGuild().getChannels().get(0);
//		novaBot.sendMessage(channel, botmsg);
	}

	private String getRandomSong() {
		File folder = new File(Config.MUSIC_DIRECTORY);
		String[] fileList = folder.list((dir, name) -> name.toLowerCase().endsWith(".mp3"));
		return fileList[(int) (Math.random() * (double) fileList.length)];
	}
}