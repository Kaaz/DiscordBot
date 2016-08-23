package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.games.Blackjack;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * !BlackJack
 * play a game of blackjack with the bot
 */
public class BlackJackCommand extends AbstractCommand {
	public BlackJackCommand(NovaBot b) {
		super(b);
	}

	private Map<String, Blackjack> playerGames = new ConcurrentHashMap<>();

	@Override
	public String getDescription() {
		return "Is being worked on!";
	}

	@Override
	public String getCommand() {
		return "blackjack";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"blackjack        //check status",
				"blackjack hit    //hits",
				"blackjack stand  //stands",
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length == 0) {
			if (playerGames.containsKey(author.getID())) {
				return "Blackjack" + Config.EOL +
						"You are still in a game. To finish type **blackjack stand**" + Config.EOL +
						"Your Current hand (" + playerGames.get(author.getID()).getValue(author.getID()) + "):" + Config.EOL +
						playerGames.get(author.getID()).printHand(author.getID());
			}
			return "You are not playing a game, to start use **blackjack hit**";
		}
		if (args[0].equalsIgnoreCase("hit")) {
			if (!playerGames.containsKey(author.getID())) {
				playerGames.put(author.getID(), new Blackjack());
			}
			playerGames.get(author.getID()).hit(author.getID());
			return "Blackjack" + Config.EOL +
					"Your Current hand (" + playerGames.get(author.getID()).getValue(author.getID()) + "):" + Config.EOL +
					playerGames.get(author.getID()).printHand(author.getID());
		} else if (args[0].equalsIgnoreCase("stand")) {
			return TextHandler.get("command_not_implemented");
		} else if (args[0].equalsIgnoreCase("reset")) {
			playerGames.put(author.getID(), new Blackjack());
			return "only this time";
		}
		return TextHandler.get("command_not_implemented");
	}
}
