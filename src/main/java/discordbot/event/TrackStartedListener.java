package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.main.NovaBot;
import sx.blah.discord.util.audio.events.TrackStartEvent;

/**
 * Track finished event
 */
public class TrackStartedListener extends AbstractEventListener<TrackStartEvent> {
	public TrackStartedListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(TrackStartEvent event) {
		novaBot.trackStarted(event.getTrack(), event.getPlayer().getGuild());
	}
}