package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

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
		String botmsg = ":notes: Track donezo!";
		int itemsInQueue = event.getPlayer().getPlaylistSize();
		AudioPlayer.Track oldTrack = event.getOldTrack();
		Optional<AudioPlayer.Track> newTrack = event.getNewTrack();
		if (newTrack.isPresent()) {
			botmsg += Config.EOL + ":notes: ";
			botmsg += ":notes: Next track: " + newTrack.get().getTotalTrackTime() + "s";
		}
		IChannel channel = event.getPlayer().getGuild().getChannels().get(0);
		novaBot.sendMessage(channel, botmsg);
	}
}