package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**
 * Successfully logged in to discord
 */
public class ReadyListener extends AbstractEventListener<ReadyEvent> {
	public ReadyListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(ReadyEvent event) {

		discordBot.markReady(true);
		System.out.println("[event] Bot is ready!");

	}

}