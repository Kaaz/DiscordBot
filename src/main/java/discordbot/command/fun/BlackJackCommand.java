package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.games.Blackjack;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

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
				final Future<?>[] f = {null};
				if (!playerGames.get(author.getId()).playerIsStanding()) {
					bot.out.sendAsyncMessage(channel, playerGames.get(author.getId()).toString(), message -> {
						playerGames.get(author.getId()).stand();
						f[0] = bot.scheduleRepeat(() -> {
							boolean didHit = playerGames.get(author.getId()).dealerHit();
							message.editMessage(playerGames.get(author.getId()).toString()).queue();

							if (!didHit) {
								playerGames.remove(author.getId());
								f[0].cancel(false);
							}
						}, DEALER_TURN_INTERVAL, DEALER_TURN_INTERVAL);
					});
				}
				return "";
			}
			return "You are not playing a game, to start use **" + DisUtil.getCommandPrefix(channel) + "blackjack hit**";
		}

		return Template.get("command_invalid_use");
	}
}