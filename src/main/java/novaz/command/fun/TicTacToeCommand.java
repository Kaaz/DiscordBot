package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.games.GameState;
import novaz.games.tictactoe.TicGameTurn;
import novaz.games.tictactoe.TicTacToeGame;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TicTacToeCommand extends AbstractCommand {
	private Map<String, TicTacToeGame> playerGames = new ConcurrentHashMap<>();
	private Map<String, String> playersToGames = new ConcurrentHashMap<>();

	public TicTacToeCommand(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Play a game of tic tac toe with someone";
	}

	@Override
	public String getCommand() {
		return "tic";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"tic new      //starts a new session and wait for someone to join ",
				"tic cancel   //cancels the current session ",
				"tic @<user>  //requests to play a game with <user> ",
				"tic 1-9      //claims the tile  ",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (channel.isPrivate()) {
			return TextHandler.get("command_not_for_private");
		}
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("new")) {
				if (!isInAGame(author.getID())) {
					TicTacToeGame game = getOrCreateGame(author.getID());
					game.addPlayer(author);
					return TextHandler.get("command_tic_game_created_waiting_for_player") + Config.EOL + game.toString();
				} else {
					return TextHandler.get("command_tic_already_in_a_game") + Config.EOL + getOrCreateGame(author.getID()).toString();
				}
			} else if (args[0].equalsIgnoreCase("cancel")) {
				if (isInAGame(author.getID())) {
					removeGame(author.getID());
					return TextHandler.get("command_tic_canceled_game");
				}
				return TextHandler.get("command_tic_not_in_game");
			} else if (Misc.isUserMention(args[0])) {
				if (isInAGame(author.getID())) {
					return TextHandler.get("command_tic_already_in_a_game");
				}
				String userId = Misc.mentionToId(args[0]);
				IUser targetUser = bot.instance.getUserByID(userId);
				if (isInAGame(targetUser.getID())) {
					TicTacToeGame otherGame = getOrCreateGame(targetUser.getID());
					if (otherGame.waitingForPlayer()) {
						otherGame.addPlayer(author);
						joinGame(author.getID(), targetUser.getID());
						return TextHandler.get("command_tic_joined_target") + Config.EOL + otherGame.toString();
					}
					return TextHandler.get("command_tic_target_already_in_a_game");
				}
				TicTacToeGame newGame = getOrCreateGame(author.getID());
				newGame.addPlayer(author);
				newGame.addPlayer(targetUser);
				joinGame(targetUser.getID(), author.getID());
				return newGame.toString();
			} else if (args[0].matches("^\\d$")) {
				int placementIndex = Integer.parseInt(args[0]) - 1;
				if (isInAGame(author.getID())) {
					TicTacToeGame game = getOrCreateGame(author.getID());
					if (game.waitingForPlayer()) {
						return TextHandler.get("command_tic_waiting_for_player");
					}
					if (!game.isTurnOf(author)) {
						return TextHandler.get("command_tic_not_your_turn");
					}
					if (!game.isValidMove(author, new TicGameTurn(placementIndex))) {
						return TextHandler.get("command_tic_not_a_valid_move");
					}
					game.playTurn(author, new TicGameTurn(placementIndex));
					String gamestr = game.toString();
					if (game.getGameState().equals(GameState.OVER)) {
						removeGame(author.getID());
					}
					return gamestr;
				}
				return TextHandler.get("command_tic_not_in_game");
			} else {
				return TextHandler.get("command_tic_invalid_usage");
			}
		}
		if (isInAGame(author.getID())) {
			return getOrCreateGame(author.getID()).toString();
		}
		return TextHandler.get("command_tic_not_in_game");
	}

	private boolean isInAGame(String playerId) {
		return playersToGames.containsKey(playerId) && playerGames.containsKey(playersToGames.get(playerId));
	}

	private boolean joinGame(String playerId, String playerHostId) {
		if (isInAGame(playerHostId)) {
			String gameId = Misc.getKeyByValue(playerGames, getOrCreateGame(playerHostId));
			playersToGames.put(playerId, gameId);
		}
		return false;
	}

	private void removeGame(String playerId) {
		String gamekey = Misc.getKeyByValue(playerGames, getOrCreateGame(playerId));
		playerGames.remove(gamekey);
		playersToGames.remove(playerId);
		String otherplayer = Misc.getKeyByValue(playersToGames, gamekey);
		if (otherplayer != null) {
			playersToGames.remove(otherplayer);
		}
	}

	private TicTacToeGame getOrCreateGame(String playerId) {
		if (!isInAGame(playerId)) {
			playerGames.put(playerId, new TicTacToeGame());
			playersToGames.put(playerId, playerId);
		}
		return playerGames.get(playersToGames.get(playerId));
	}
}