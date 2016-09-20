package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.main.NovaBot;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**
 * Successfully logged in to discord
 */
public class ReadyListener extends AbstractEventListener<ReadyEvent> {
	public ReadyListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(ReadyEvent event) {

		novaBot.markReady(true);
		System.out.println("[event] Bot is ready!");

	}

}