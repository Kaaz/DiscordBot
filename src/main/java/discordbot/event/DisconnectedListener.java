package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.core.ExitCode;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;

/**
 * Whenever the bot disconnects
 */
public class DisconnectedListener extends AbstractEventListener<DiscordDisconnectedEvent> {
	public DisconnectedListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(DiscordDisconnectedEvent event) {
		DiscordBot.LOGGER.info("[event] DISCONNECTED! ");
		if (event.getReason().equals(DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED)) {
			Launcher.stop(ExitCode.DISCONNECTED);
		}
	}

}