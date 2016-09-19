package novaz.handler;

import novaz.games.AbstractGame;
import novaz.games.GameState;
import novaz.games.GameTurn;
import novaz.guildsettings.defaults.SettingGameModule;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.DisUtil;
import novaz.util.Misc;
import org.reflections.Reflections;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameHandler {

	private final NovaBot bot;
	private Map<String, AbstractGame> playerGames = new ConcurrentHashMap<>();
	private Map<String, String> playersToGames = new ConcurrentHashMap<>();
	private final Map<String, Class<? extends AbstractGame>> gameClassMap;
	private final Map<String, AbstractGame> gameInfoMap;
	private final Map<String, IMessage> lastMessage;
	private Map<String, String> usersInPlayMode;
	private static final String COMMAND_NAME = "game";

	private boolean isInPlayMode(IUser user, IChannel channel) {
		return usersInPlayMode.containsKey(user.getID()) && usersInPlayMode.get(user.getID()).equals(channel.getID());
	}

	private void enterPlayMode(IChannel channel, IUser player) {
		usersInPlayMode.put(player.getID(), channel.getID());
	}

	private void leavePlayMode(IUser player) {
		if (usersInPlayMode.containsKey(player.getID())) {
			usersInPlayMode.remove(player.getID());
		}
	}

	public boolean isGameInput(IChannel channel, IUser player, String message) {
		if (GuildSettings.getFor(channel, SettingGameModule.class).equals("true")) {
			if (isInPlayMode(player, channel) || message.startsWith(DisUtil.getCommandPrefix(channel) + COMMAND_NAME)) {
				return true;
			}
		}
		return false;
	}

	public GameHandler(NovaBot bot) {
		this.bot = bot;
		gameClassMap = new HashMap<>();
		gameInfoMap = new HashMap<>();
		lastMessage = new ConcurrentHashMap<>();
		usersInPlayMode = new ConcurrentHashMap<>();
		collectGameClasses();
	}

	public final void execute(IUser player, IChannel channel, String rawMessage) {
		String message = rawMessage.toLowerCase().trim();
		if (!isInPlayMode(player, channel)) {
			message = message.replace(DisUtil.getCommandPrefix(channel) + COMMAND_NAME, "").trim();
		}
		switch (message) {
			case "playmode":
			case "enter":
			case "play":
				enterPlayMode(channel, player);
				bot.out.sendMessage(channel, TextHandler.get("playmode_entering_mode"));
				return;
			case "exit":
			case "leave":
			case "stop":
				leavePlayMode(player);
				bot.out.sendMessage(channel, TextHandler.get("playmode_leaving_mode"));
				return;
			default:
				break;
		}
		String[] args = message.split(" ");
		String gameMessage = executeGameMove(args, player, channel);
		if (isInPlayMode(player, channel)) {
			gameMessage = "*note: " + TextHandler.get("playmode_in_mode_warning") + "*" + Config.EOL + gameMessage;
		} else if ("".equals(message) || "help".equals(message)) {
			gameMessage = showList(channel);
		}
		if (!gameMessage.isEmpty()) {
			IMessage msg = bot.out.sendMessage(channel, gameMessage);
			if (lastMessage.containsKey(channel.getID())) {
				IMessage msgToDelete = lastMessage.remove(channel.getID());
				bot.out.deleteMessage(msgToDelete);
			}
			lastMessage.put(channel.getID(), msg);
		}
	}

	private void collectGameClasses() {
		Reflections reflections = new Reflections("novaz.games");
		Set<Class<? extends AbstractGame>> classes = reflections.getSubTypesOf(AbstractGame.class);
		for (Class<? extends AbstractGame> gameClass : classes) {
			try {
				AbstractGame abstractGame = gameClass.getConstructor().newInstance();
				gameClassMap.put(abstractGame.getCodeName(), gameClass);
				gameInfoMap.put(abstractGame.getCodeName(), abstractGame);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private String getFormattedGameList() {
		List<List<String>> table = new ArrayList<>();

		getGameList().forEach(game -> {
			List<String> row = new ArrayList<>();
			row.add(game.getCodeName());
			row.add(game.getFullname());
			table.add(row);
		});
		return Misc.makeAsciiTable(Arrays.asList("code", "gamename"), table);
	}

	public List<AbstractGame> getGameList() {
		List<AbstractGame> gamelist = new ArrayList<>();
		gamelist.addAll(gameInfoMap.values());
		return gamelist;
	}

	private AbstractGame createGameInstance(String gameCode) {
		try {
			return gameClassMap.get(gameCode).getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String createGame(IUser player, String gameCode) {
		if (!isInAGame(player.getID())) {
			if (gameClassMap.containsKey(gameCode)) {
				AbstractGame gameInstance = createGameInstance(gameCode);
				if (gameInstance == null) {
					return TextHandler.get("playmode_cant_create_instance");
				}
				if (createGame(player.getID(), gameInstance)) {
					return TextHandler.get("playmode_cant_register_instance");
				}
				gameInstance.addPlayer(player);
				if (gameInstance.waitingForPlayer()) {
					return TextHandler.get("playmode_created_waiting_for_player") + Config.EOL + gameInstance.toString();
				}
				return gameInstance.toString();
			}
			return TextHandler.get("playmode_invalid_gamecode");
		}
		return TextHandler.get("playmode_already_in_game") + Config.EOL + getGame(player.getID());
	}

	private String cancelGame(IUser player) {
		if (isInAGame(player.getID())) {
			removeGame(player.getID());
			return TextHandler.get("playmode_canceled_game");
		}
		return TextHandler.get("playmode_not_in_game");
	}

	private String createGamefromUserMention(IUser player, String theMention, String gamecode) {
		if (isInAGame(player.getID())) {
			return TextHandler.get("playmode_already_in_game");
		}
		String userId = DisUtil.mentionToId(theMention);
		IUser targetUser = bot.instance.getUserByID(userId);
		if (isInAGame(targetUser.getID())) {
			AbstractGame otherGame = getGame(targetUser.getID());
			if (otherGame != null && otherGame.waitingForPlayer()) {
				otherGame.addPlayer(player);
				joinGame(player.getID(), targetUser.getID());
				return TextHandler.get("playmode_joined_target") + Config.EOL + otherGame.toString();
			}
			return TextHandler.get("playmode_target_already_in_a_game");
		}
		if (!gameClassMap.containsKey(gamecode)) {
			return TextHandler.get("playmode_invalid_gamecode");
		}

		AbstractGame newGame = createGameInstance(gamecode);
		if (newGame == null) {
			return TextHandler.get("playmode_cant_create_instance");
		}
		createGame(player.getID(), newGame);
		newGame.addPlayer(player);
		newGame.addPlayer(targetUser);
		joinGame(targetUser.getID(), player.getID());
		return newGame.toString();
	}

	private String showHelp() {
		return "Type list for a list of games, TODO FOR NOW";
	}

	private String showList(IChannel channel) {
		return "A list of all available games" + Config.EOL +
				getFormattedGameList() +
				"to start one type `" + DisUtil.getCommandPrefix(channel) + COMMAND_NAME + " <@user> <gamecode>`" + Config.EOL +
				"You can enter *gamemode* by typing `" + DisUtil.getCommandPrefix(channel) + COMMAND_NAME + " enter` " + Config.EOL +
				"This makes it so that you don't have to prefix your messages with `" + DisUtil.getCommandPrefix(channel) + COMMAND_NAME + "`";
	}

	public String executeGameMove(String[] args, IUser player, IChannel channel) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("new") && args.length > 1) {
//				return createGame(player, args[1]);
			} else if (args[0].equalsIgnoreCase("cancel")) {
				return cancelGame(player);
			} else if (args[0].equalsIgnoreCase("help")) {
				return showHelp();
			} else if (args[0].equalsIgnoreCase("list")) {
				return showList(channel);
			} else if (DisUtil.isUserMention(args[0])) {
				if (args.length > 1) {
					return createGamefromUserMention(player, args[0], args[1]);
				}
				return TextHandler.get("playmode_invalid_usage");
			}
			return playTurn(player, args[0]);
		}
		if (isInAGame(player.getID())) {
			return String.valueOf(getGame(player.getID()));
		}
		return TextHandler.get("playmode_not_in_game");
	}

	private String playTurn(IUser player, String input) {
		if (isInAGame(player.getID())) {
			AbstractGame game = getGame(player.getID());
			if (game == null) {
				return TextHandler.get("playmode_game_corrupt");
			}
			if (game.waitingForPlayer()) {
				return TextHandler.get("playmode_waiting_for_player");
			}
			if (!game.isTurnOf(player)) {
				return game.toString() + Config.EOL + TextHandler.get("playmode_not_your_turn");
			}
			GameTurn gameTurnInstance = game.getGameTurnInstance();
			if (gameTurnInstance == null) {
				return "BEEP BOOP CONTACT KAAZ THIS SHIT IS ON FIRE **game.getGameTurnInstance()** failed somehow";
			}
			if (!gameTurnInstance.parseInput(input)) {
				return game.toString() + Config.EOL + ":exclamation: " + gameTurnInstance.getInputErrorMessage();
			}
			if (!game.isValidMove(player, gameTurnInstance)) {
				return game.toString() + Config.EOL + TextHandler.get("playmode_not_a_valid_move");
			}
			game.playTurn(player, gameTurnInstance);
			String gamestr = game.toString();
			if (game.getGameState().equals(GameState.OVER)) {
				removeGame(player.getID());
			}
			return gamestr;
		}
		return TextHandler.get("playmode_not_in_game");
	}

	private boolean isInAGame(String playerId) {
		return playersToGames.containsKey(playerId) && playerGames.containsKey(playersToGames.get(playerId));
	}

	private boolean joinGame(String playerId, String playerHostId) {
		if (isInAGame(playerHostId)) {
			String gameId = Misc.getKeyByValue(playerGames, getGame(playerHostId));
			playersToGames.put(playerId, gameId);
		}
		return false;
	}

	private void removeGame(String playerId) {
		String gamekey = Misc.getKeyByValue(playerGames, getGame(playerId));
		playerGames.remove(gamekey);
		playersToGames.remove(playerId);
		String otherplayer = Misc.getKeyByValue(playersToGames, gamekey);
		if (otherplayer != null) {
			playersToGames.remove(otherplayer);
		}
	}

	private AbstractGame getGame(String playerId) {
		if (isInAGame(playerId)) {
			return playerGames.get(playersToGames.get(playerId));
		}
		return null;
	}

	private boolean createGame(String playerId, AbstractGame game) {
		if (!isInAGame(playerId)) {
			playerGames.put(playerId, game);
			playersToGames.put(playerId, playerId);
			return true;
		}
		return false;
	}
}
