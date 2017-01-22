package discordbot.command.fun;

import discordbot.command.CommandReactionListener;
import discordbot.command.ICommandReactionListener;
import discordbot.core.AbstractCommand;
import discordbot.games.game2048.Game2048;
import discordbot.games.game2048.Game2048Turn;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2017-01-21.
 */
public class TwoZeroFourZeroCommand extends AbstractCommand implements ICommandReactionListener<Game2048> {
	private Game2048 game = null;

	@Override
	public String getDescription() {
		return "play a game of 2048";
	}

	@Override
	public String getCommand() {
		return "2048";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"2048       //play the game",
				"2048 reset //resets the game in progress"};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (!author.getId().equals(Config.CREATOR_ID)) {
			return "";
		}
		if (game == null) {
			game = new Game2048();
			game.addPlayer(author);
			channel.sendMessage(game.toString()).queue(
					message -> bot.commandReactionHandler.addReactionListener(((TextChannel) channel).getGuild().getId(), message, getReactionListener(author.getId(), game))

			);
			return "";
		}
		return game.toString();

	}

	@Override
	public CommandReactionListener<Game2048> getReactionListener(String invoker, Game2048 game) {
		CommandReactionListener<Game2048> listener = new CommandReactionListener<>(invoker, game);
		listener.setExpiresIn(TimeUnit.MINUTES, 10);
		for (String reaction : game.getReactions()) {
			listener.registerReaction(Emojibet.getEmojiFor(reaction), message -> {
				Game2048Turn turn = new Game2048Turn();
				turn.parseInput(reaction);
				if (!game.isValidMove(message.getJDA().getUserById(invoker), turn)) {
					message.editMessage(game.toString() + Config.EOL + Template.get("playmode_not_a_valid_move")).queue();
				} else {
					game.playTurn(message.getJDA().getUserById(invoker), turn);
					message.editMessage(game.toString()).queue();
				}
			});
		}
		return listener;
	}
}
