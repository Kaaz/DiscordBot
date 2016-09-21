package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.main.DiscordBot;
import sx.blah.discord.util.audio.events.TrackStartEvent;

/**
 * Track finished event
 */
public class TrackStartedListener extends AbstractEventListener<TrackStartEvent> {
	public TrackStartedListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(TrackStartEvent event) {
		discordBot.trackStarted(event.getTrack(), event.getPlayer().getGuild());
	}
}