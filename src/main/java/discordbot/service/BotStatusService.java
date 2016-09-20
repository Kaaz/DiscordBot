package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.main.Config;
import discordbot.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.Random;

/**
 * pseudo randmoly sets the now playing tag of the bot
 */
public class BotStatusService extends AbstractService {
	private final static Status[] statusList = {
			Status.game("with human pets"),
			Status.game("Teaching Minions"),
			Status.game("Planking"),
			Status.game("Bot simulator 2015"),
			Status.game("Pokemon Go"),
			Status.game("Cow tipping"),
			Status.game("Sorting commands"),
			Status.game("Planning for wold domination"),
			Status.game("Reading wikipedia"),
			Status.game("Talking to Martians"),
			Status.game("Homework"),
			Status.game("Hearthstone"),
			Status.game("Path of exile"),
			Status.game("Blackjack"),
			Status.game("Looking for new jokes"),
			Status.game("Organizing music"),
			Status.game("Trying to remember preferences"),
			Status.game("Analyzing fellow humans"),
			Status.game("Yesterday you said tomorrow"),
			Status.game("Let dreams be dreams"),
			Status.game("Rare pepe"),
	};

	public BotStatusService(NovaBot b) {
		super(b);
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
		return !bot.statusLocked;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		IChannel channel = bot.instance.getChannelByID(Config.BOT_CHANNEL_ID);
		try {
			List<IInvite> invites = channel.getInvites();
			if (invites.size() > 0) {
				if (new Random().nextInt(100) < 20) {
					bot.instance.changeStatus(Status.game("Feedback @ https://discord.gg/" + invites.get(0).getInviteCode()));
					return;
				}
			} else {
				bot.out.sendPrivateMessage(bot.instance.getUserByID(Config.CREATOR_ID), ":exclamation: I am out of invites for `" + channel.getName() + "` Click here to make more :D " + channel.mention());
			}
		} catch (DiscordException | RateLimitException | MissingPermissionsException e) {
			bot.out.sendErrorToMe(e, "mychannel", Config.BOT_CHANNEL_ID, "invite_error", ":sob:");
			e.printStackTrace();
		}
		bot.instance.changeStatus(statusList[new Random().nextInt(statusList.length)]);
	}

	@Override
	public void afterRun() {
	}
}