package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.main.DiscordBot;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

/**
 * Track finished event
 */
public class TrackFinishedListener extends AbstractEventListener<TrackFinishEvent> {
	public TrackFinishedListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(TrackFinishEvent event) {
		discordBot.trackEnded(event.getOldTrack(), event.getNewTrack(), event.getPlayer().getGuild());
	}
}