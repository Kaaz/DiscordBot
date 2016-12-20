package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

/**
 * pseudo randomly sets the now playing tag of the bot
 */
public class BotStatusService extends AbstractService {
	private final static String[] statusList = {
			"with human pets",
			"Teaching Minions",
			"Planking",
			"Bot simulator 2015",
			"Pokemon Go",
			"Cow tipping",
			"Sorting commands",
			"Planning for wold domination",
			"Reading wikipedia",
			"Talking to Martians",
			"Homework",
			"Hearthstone",
			"Path of exile",
			"Blackjack",
			"Half Life 3",
			"russian roulette",
			"hide and seek",
			"peekaboo",
			"\";DROP TABLE",
			"rating your waifu",
			"Talking to idiots",
			"Looking for new jokes",
			"Organizing music",
			"Trying to remember preferences",
			"Analyzing fellow humans",
			"Yesterday you said tomorrow",
			"Let dreams be dreams",
			"Rare pepe"
	};
	private final Random rng;

	public BotStatusService(BotContainer b) {
		super(b);
		rng = new Random();
	}

	@Override
	public String getIdentifier() {
		return "bot_nickname";
	}

	@Override
	public long getDelayBetweenRuns() {
		return 300_000;
	}

	@Override
	public boolean shouldIRun() {
		return !bot.isStatusLocked();
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		int roll = rng.nextInt(100);
		String statusText;
		TextChannel inviteChannel = bot.getBotFor(Config.BOT_GUILD_ID).client.getTextChannelById(Config.BOT_CHANNEL_ID);
		if (inviteChannel != null && roll <= 5) {
			statusText = "Feedback @ https://discord.gg/eaywDDt";
		} else if (roll < 25) {
			String username = bot.getShards()[0].client.getSelfUser().getName();
			statusText = "@" + username + " help || @" + username + " invite";
		} else {
			statusText = statusList[new Random().nextInt(statusList.length)];
		}

		for (DiscordBot shard : bot.getShards()) {
			shard.client.getPresence().setGame(Game.of(statusText));
		}
	}

	@Override
	public void afterRun() {
	}
}