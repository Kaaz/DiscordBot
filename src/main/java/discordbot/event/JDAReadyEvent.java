package discordbot.event;

import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class JDAReadyEvent extends ListenerAdapter {
	private DiscordBot discordBot;

	public JDAReadyEvent(DiscordBot bot) {
		this.discordBot = bot;
	}


	@Override
	public void onReady(ReadyEvent event) {
		discordBot.markReady();
		System.out.println("[event] Bot is ready!");
	}

	@Override
	public void onReconnect(ReconnectedEvent event) {
		discordBot.markReady();
	}
}