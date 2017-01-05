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
		TextChannel inviteChannel = bot.getShardFor(Config.BOT_GUILD_ID).client.getTextChannelById(Config.BOT_CHANNEL_ID);
		if (inviteChannel != null && roll < 10) {
			String fallback = "Feedback @ https://discord.gg/eaywDDt";
			inviteChannel.getInvites().queue(invites -> {
				if (invites != null && !invites.isEmpty()) {
					setGameOnShards(bot, "Feedback @ https://discord.gg/" + invites.get(0).getCode());
				} else {
					setGameOnShards(bot, fallback);
				}
			}, throwable -> setGameOnShards(bot, fallback));

		} else if (roll < 50) {
			String username = bot.getShards()[0].client.getSelfUser().getName();
			setGameOnShards(bot, "@" + username + " help || @" + username + " invite");
		} else {
			setGameOnShards(bot, statusList[new Random().nextInt(statusList.length)]);
		}
	}

	private void setGameOnShards(BotContainer container, String status) {
		for (DiscordBot shard : container.getShards()) {
			shard.client.getPresence().setGame(Game.of(status));
		}
	}

	@Override
	public void afterRun() {
	}
}