package novaz.service;

import novaz.core.AbstractService;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.Status;

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
		return 600_000;
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
		bot.instance.changeStatus(statusList[new Random().nextInt(statusList.length)]);
	}

	@Override
	public void afterRun() {
	}
}