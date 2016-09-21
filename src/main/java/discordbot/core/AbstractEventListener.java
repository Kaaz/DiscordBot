package discordbot.core;

import discordbot.main.DiscordBot;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;

public abstract class AbstractEventListener<eventType extends Event> implements IListener<eventType> {
	protected DiscordBot discordBot;

	public AbstractEventListener(DiscordBot discordBot) {
		this.discordBot = discordBot;
	}

	abstract public boolean listenerIsActivated();
}
