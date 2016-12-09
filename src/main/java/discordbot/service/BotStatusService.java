package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.InviteUtil;

import java.util.List;
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
		return 120_000;
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
		String statusText = "Something";
		TextChannel inviteChannel = bot.getBotFor(Config.BOT_GUILD_ID).client.getTextChannelById(Config.BOT_CHANNEL_ID);
		if (inviteChannel != null && roll <= 5) {
			List<InviteUtil.AdvancedInvite> invites = inviteChannel.getInvites();
			if (invites.size() > 0) {
				statusText = "Feedback @ https://discord.gg/" + invites.get(0).getCode();
			} else {
				bot.getBotFor(Config.BOT_GUILD_ID).out.sendMessageToCreator(":exclamation: I am out of invites for `" + inviteChannel.getName() + "` Click here to make more :D " + inviteChannel.getAsMention());
			}
		} else if (roll <= 15) {
			String username = bot.getShards()[0].client.getSelfInfo().getUsername();
			statusText = "@" + username + " help || @" + username + " invite";
		} else {
			statusText = statusList[new Random().nextInt(statusList.length)];
		}

		for (DiscordBot shard : bot.getShards()) {
			shard.client.getAccountManager().setGame(statusText);
		}
	}

	@Override
	public void afterRun() {
	}
}