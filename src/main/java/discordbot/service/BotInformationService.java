package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.List;

/**
 * updates information posts about the bot and clears the channel (if possible)
 */
public class BotInformationService extends AbstractService {

	public BotInformationService(DiscordBot b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_information_display";
	}

	@Override
	public long getDelayBetweenRuns() {
		return 86400000L;
	}

	@Override
	public boolean shouldIRun() {
		return true;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		List<TextChannel> subscribedChannels = getSubscribedChannels();
		User me = bot.client.getSelfInfo();
		for (TextChannel channel : subscribedChannels) {
			bot.commands.getCommand("purge").execute(new String[]{}, channel, me);
			bot.out.sendAsyncMessage(channel, Template.get("bot_service_information_display_title"), null);
			bot.out.sendAsyncMessage(channel, bot.commands.getCommand("info").execute(new String[]{}, channel, me), null);
			bot.out.sendAsyncMessage(channel, bot.commands.getCommand("help").execute(new String[]{}, channel, me), null);
		}
	}

	@Override
	public void afterRun() {
	}
}