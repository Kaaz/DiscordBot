package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.main.NovaBot;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

/**
 * Track finished event
 */
public class TrackFinishedListener extends AbstractEventListener<TrackFinishEvent> {
	public TrackFinishedListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(TrackFinishEvent event) {
		novaBot.trackEnded(event.getOldTrack(), event.getNewTrack(), event.getPlayer().getGuild());
	}
}