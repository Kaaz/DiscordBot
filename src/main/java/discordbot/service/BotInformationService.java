package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.handler.Template;
import discordbot.main.BotContainer;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.List;

/**
 * updates information posts about the bot and clears the channel (if possible)
 */
public class BotInformationService extends AbstractService {

	public BotInformationService(BotContainer b) {
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
		return false;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		List<TextChannel> subscribedChannels = getSubscribedChannels();
		DiscordBot bot = this.bot.getShards()[0];
		User me = bot.client.getSelfInfo();
		for (TextChannel channel : subscribedChannels) {
			this.bot.getBotFor(channel.getGuild().getId()).commands.getCommand("purge").execute(new String[]{}, channel, me);
			sendTo(channel, Template.get("bot_service_information_display_title"));
			sendTo(channel, bot.commands.getCommand("info").execute(new String[]{}, channel, me));
			sendTo(channel, bot.commands.getCommand("help").execute(new String[]{}, channel, me));
		}
	}

	@Override
	public void afterRun() {
	}
}