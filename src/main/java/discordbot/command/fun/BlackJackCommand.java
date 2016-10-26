package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.games.Blackjack;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * !BlackJack
 * play a game of blackjack with the bot
 */
public class BlackJackCommand extends AbstractCommand {
	public final long DEALER_TURN_INTERVAL = 2000L;
	private Map<String, Blackjack> playerGames = new ConcurrentHashMap<>();

	public BlackJackCommand() {
		super();
	}

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
		return new String[]{
				"bj"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (args.length == 0) {
			if (playerGames.containsKey(author.getId()) && playerGames.get(author.getId()).isInProgress()) {
				return "You are still in a game. To finish type **blackjack stand**" + Config.EOL +
						playerGames.get(author.getId()).toString();
			}
			return "You are not playing a game, to start use **" + DisUtil.getCommandPrefix(channel) + "blackjack hit**";
		}
		if (args[0].equalsIgnoreCase("hit")) {
			if (!playerGames.containsKey(author.getId()) || !playerGames.get(author.getId()).isInProgress()) {
				playerGames.put(author.getId(), new Blackjack(author.getAsMention()));
			}
			if (playerGames.get(author.getId()).isInProgress() && !playerGames.get(author.getId()).playerIsStanding()) {
				playerGames.get(author.getId()).hit();
				return playerGames.get(author.getId()).toString();
			}
			return "";
		} else if (args[0].equalsIgnoreCase("stand")) {
			if (playerGames.containsKey(author.getId())) {
				if (!playerGames.get(author.getId()).playerIsStanding()) {
					bot.out.sendAsyncMessage(channel, playerGames.get(author.getId()).toString(), message -> {
						playerGames.get(author.getId()).stand();
						bot.timer.scheduleAtFixedRate(new TimerTask() {
							@Override
							public void run() {
								try {
									boolean didHit = playerGames.get(author.getId()).dealerHit();
									message.updateMessageAsync(playerGames.get(author.getId()).toString(), null);

									if (!didHit) {
										playerGames.remove(author.getId());
										this.cancel();
									}
								} catch (Exception e) {
									bot.out.sendErrorToMe(e, "blackjackgame", author.getId(), bot);
									this.cancel();
									playerGames.remove(author.getId());
								}
							}
						}, 1000L, DEALER_TURN_INTERVAL);
					});
				}
				return "";
			}
			return "You are not playing a game, to start use **" + DisUtil.getCommandPrefix(channel) + "blackjack hit**";
		}

		return Template.get("command_invalid_use");
	}
}