package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.games.Blackjack;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * !BlackJack
 * play a game of blackjack with the bot
 */
public class BlackJackCommand extends AbstractCommand {
	public final long DEALER_TURN_INTERVAL = 2000L;

	public BlackJackCommand(NovaBot b) {
		super(b);
	}

	private Map<String, Blackjack> playerGames = new ConcurrentHashMap<>();

	@Override
	public String getDescription() {
		return "play a game of blackjack!";
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
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length == 0) {
			if (playerGames.containsKey(author.getID()) && playerGames.get(author.getID()).isInProgress()) {
				return "You are still in a game. To finish type **blackjack stand**" + Config.EOL +
						playerGames.get(author.getID()).toString();
			}
			return "You are not playing a game, to start use **blackjack hit**";
		}
		if (args[0].equalsIgnoreCase("hit")) {
			if (!playerGames.containsKey(author.getID()) || !playerGames.get(author.getID()).isInProgress()) {
				playerGames.put(author.getID(), new Blackjack(author.mention()));
			}
			if (playerGames.get(author.getID()).isInProgress() && !playerGames.get(author.getID()).playerIsStanding()) {
				playerGames.get(author.getID()).hit();
				return playerGames.get(author.getID()).toString();
			}
			return "";
		} else if (args[0].equalsIgnoreCase("stand")) {
			if (playerGames.containsKey(author.getID())) {
				if (!playerGames.get(author.getID()).playerIsStanding()) {
					IMessage msg = bot.sendMessage(channel, playerGames.get(author.getID()).toString());
					playerGames.get(author.getID()).stand();
					bot.timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							try {
								boolean didHit = playerGames.get(author.getID()).dealerHit();
								msg.edit(playerGames.get(author.getID()).toString());
								if (!didHit) {
									playerGames.remove(author.getID());
									this.cancel();
								}
							} catch (DiscordException e) {
								if (!e.getErrorMessage().contains("502")) {
									bot.sendErrorToMe(e, "blackjackgame", author.getID());
								}
							} catch (Exception e) {
								bot.sendErrorToMe(e, "blackjackgame", author.getID());
								this.cancel();
								playerGames.remove(author.getID());
							}
						}
					}, 1000L, DEALER_TURN_INTERVAL);
				}
				return "";
			}
			return "You are not playing a game, to start use **blackjack hit**";
		}

		return TextHandler.get("command_invalid_use");
	}
}