package discordbot.handler;

import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandCategory;
import discordbot.command.CommandReactionListener;
import discordbot.command.CommandVisibility;
import discordbot.command.ICommandCooldown;
import discordbot.command.ICommandReactionListener;
import discordbot.core.AbstractCommand;
import discordbot.db.WebDb;
import discordbot.db.controllers.CBlacklistCommand;
import discordbot.db.controllers.CBotEvent;
import discordbot.db.controllers.CCommandCooldown;
import discordbot.db.controllers.CCommandLog;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OBlacklistCommand;
import discordbot.db.model.OBotEvent;
import discordbot.db.model.OCommandCooldown;
import discordbot.guildsettings.bot.SettingCommandPrefix;
import discordbot.guildsettings.bot.SettingShowUnknownCommands;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.DisUtil;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles all the commands
 */
public class CommandHandler {

	private static final HashMap<String, AbstractCommand> commands = new HashMap<>();
	private static final HashMap<String, AbstractCommand> commandsAlias = new HashMap<>();
	private static final Map<String, String> customCommands = new ConcurrentHashMap<>();
	private static final Map<Integer, Map<String, String>> guildCommands = new ConcurrentHashMap<>();
	private static final Map<Integer, Map<String, Map<String, Boolean>>> commandBlacklist = new ConcurrentHashMap<>();

	/**
	 * checks if the the message in channel is a command
	 *
	 * @param channel        the channel the message came from
	 * @param msg            the message
	 * @param mentionMe      the user mention string
	 * @param mentionMeAlias the nickname
	 * @return whether or not the message is a command
	 */
	public static boolean isCommand(TextChannel channel, String msg, String mentionMe, String mentionMeAlias) {
		return msg.startsWith(DisUtil.getCommandPrefix(channel)) || msg.startsWith(mentionMe) || msg.startsWith(mentionMeAlias);
	}

	public static void removeGuild(int guildId) {
		if (guildCommands.containsKey(guildId)) {
			guildCommands.remove(guildId);
		}
	}

	/**
	 * directs the command to the right class
	 *
	 * @param bot             The bot instance
	 * @param channel         which channel
	 * @param author          author
	 * @param incomingMessage message
	 */
	public static void process(DiscordBot bot, MessageChannel channel, User author, String incomingMessage) {
		String outMsg = "";
		boolean commandSuccess = true;
		boolean startedWithMention = false;
		int guildId = 0;
		String inputMessage = incomingMessage;
		String commandUsed = "-";
		if (inputMessage.startsWith(bot.mentionMe)) {
			inputMessage = inputMessage.replace(bot.mentionMe, "").trim();
			startedWithMention = true;
		} else if (inputMessage.startsWith(bot.mentionMeAlias)) {
			inputMessage = inputMessage.replace(bot.mentionMeAlias, "").trim();
			startedWithMention = true;
		}

		if (channel instanceof TextChannel) {
			guildId = CGuild.getCachedId(((TextChannel) channel).getGuild().getId());
			if (!((TextChannel) channel).canTalk()) {
				return;
			}
		}
		String[] input = inputMessage.split("\\s+");
		String args[] = new String[input.length - 1];
		input[0] = DisUtil.filterPrefix(input[0], channel).toLowerCase();
		System.arraycopy(input, 1, args, 0, input.length - 1);
		CommandReactionListener<?> commandReactionListener = null;
		if (commands.containsKey(input[0]) || commandsAlias.containsKey(input[0])) {
			AbstractCommand command = commands.containsKey(input[0]) ? commands.get(input[0]) : commandsAlias.get(input[0]);
			commandUsed = command.getCommand();
			long cooldown = getCommandCooldown(command, author, channel);
			if (command.canBeDisabled() && isDisabled(guildId, channel.getId(), command.getCommand())) {
				commandSuccess = false;
				if (GuildSettings.getFor(channel, SettingShowUnknownCommands.class).equals("true")) {
					outMsg = Template.get("command_is_blacklisted", input[0]);
				}
			} else if (hasRightVisibility(channel, command.getVisibility()) && cooldown <= 0) {
				String commandOutput;
				if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
					commandOutput = commands.get("help").execute(bot, new String[]{input[0]}, channel, author);
				} else {
					commandOutput = command.execute(bot, args, channel, author);
					if (command instanceof ICommandReactionListener) {
						commandReactionListener = ((ICommandReactionListener) command).getListenObject();
					}
				}
				if (!commandOutput.isEmpty()) {
					outMsg = commandOutput;
				}
				if (Config.BOT_COMMAND_LOGGING) {
					StringBuilder usedArguments = new StringBuilder();
					for (String arg : args) {
						usedArguments.append(arg).append(" ");
					}
					if (channel instanceof TextChannel) {
						CCommandLog.saveLog(CUser.getCachedId(author.getId(), EmojiParser.parseToAliases(author.getName())),
								CGuild.getCachedId(((TextChannel) channel).getGuild().getId()),
								command.getCommand(),
								EmojiParser.parseToAliases(usedArguments.toString()).trim());
					}
				}
			} else if (cooldown > 0) {
				outMsg = Template.get("command_on_cooldown", TimeUtil.getRelativeTime((System.currentTimeMillis() / 1000L) + cooldown, false));
			} else if (!hasRightVisibility(channel, command.getVisibility())) {
				if (channel instanceof PrivateChannel) {
					outMsg = Template.get("command_not_for_private");
				} else {
					outMsg = Template.get("command_not_for_public");
				}
			}
		} else if (customCommands.containsKey(input[0])) {
			commandUsed = input[0];
			outMsg = DisUtil.replaceTags(customCommands.get(input[0]), author, channel, args);
		} else if (guildCommands.containsKey(guildId) && guildCommands.get(guildId).containsKey(input[0])) {
			commandUsed = "custom:" + input[0];
			outMsg = DisUtil.replaceTags(guildCommands.get(guildId).get(input[0]), author, channel, args);
		} else if (startedWithMention && Config.BOT_CHATTING_ENABLED) {
			commandSuccess = false;
			channel.sendTyping();
			outMsg = author.getAsMention() + ", " + bot.chatBotHandler.chat(inputMessage);
		} else if (Config.BOT_COMMAND_SHOW_UNKNOWN ||
				GuildSettings.getFor(channel, SettingShowUnknownCommands.class).equals("true")) {
			commandSuccess = false;
			outMsg = Template.get("unknown_command", GuildSettings.getFor(channel, SettingCommandPrefix.class) + "help");
		}
		if (!outMsg.isEmpty()) {
			if (commandReactionListener != null && channel instanceof TextChannel &&
					PermissionUtil.checkPermission(
							(TextChannel) channel,
							((TextChannel) channel).getGuild().getSelfMember(),
							Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS)) {
				final CommandReactionListener<?> finalCommandReactionListener = commandReactionListener;

				bot.out.sendAsyncMessage(channel, outMsg, message -> bot.commandReactionHandler.addReactionListener(((TextChannel)channel).getGuild().getId(),message, finalCommandReactionListener));

			} else {
				bot.out.sendAsyncMessage(channel, outMsg);
			}
		}
		if (commandSuccess) {
			if (channel instanceof TextChannel) {
				TextChannel tc = (TextChannel) channel;
				Launcher.log("command executed", "bot", "command",
						"input", incomingMessage,
						"user-id", author.getId(),
						"command", commandUsed,
						"user-name", author.getName(),
						"guild-id", tc.getGuild().getId(),
						"guild-name", tc.getGuild().getName(),
						"response", outMsg);
			} else {
				Launcher.log("command executed", "bot", "command-private",
						"input", incomingMessage,
						"user-id", author.getId(),
						"command", commandUsed,
						"user-name", author.getName(),
						"response", outMsg);
			}
			CUser.registerCommandUse(CUser.getCachedId(author.getId()));
		}

	}

	private static boolean hasRightVisibility(MessageChannel channel, CommandVisibility visibility) {
		if (channel instanceof PrivateChannel) {
			return visibility.isForPrivate();
		}
		return visibility.isForPublic();
	}

	/**
	 * checks if a command is on cooldown and returns the amount of seconds left before next usage
	 *
	 * @param command the command
	 * @param author  the user who sent the command
	 * @param channel the channel
	 * @return seconds till next use
	 */
	private static long getCommandCooldown(AbstractCommand command, User author, MessageChannel channel) {
		if (command instanceof ICommandCooldown) {
			long now = System.currentTimeMillis() / 1000L;
			ICommandCooldown cd = (ICommandCooldown) command;
			String targetId;
			switch (cd.getScope()) {
				case USER:
					targetId = author.getId();
					break;
				case CHANNEL:
					targetId = channel.getId();
					break;
				case GUILD:
					if (channel instanceof PrivateChannel) {
						CBotEvent.insert(OBotEvent.Level.WARN, ":warning:", ":keyboard:", String.format("`%s` issued the `%s` Command with guild-scale cooldown in private channel!", author.getName(), command.getCommand()));
					}
					targetId = ((TextChannel) channel).getGuild().getId();
					break;
				case GLOBAL:
					targetId = "GLOBAL";
					break;
				default:
					targetId = "";
					break;
			}
			OCommandCooldown cooldown = CCommandCooldown.findBy(command.getCommand(), targetId, cd.getScope().getId());
			if (cooldown.lastTime + cd.getCooldownDuration() <= now) {

				cooldown.command = command.getCommand();
				cooldown.targetId = targetId;
				cooldown.targetType = cd.getScope().getId();
				cooldown.lastTime = now;
				CCommandCooldown.insertOrUpdate(cooldown);
				return 0;
			}
			return cooldown.lastTime + cd.getCooldownDuration() - now;
		}
		return 0;
	}

	private static boolean isDisabled(int guildId, String channelId, String commandName) {
		if (guildId == 0) {
			return false;
		}
		if (!commandBlacklist.containsKey(guildId)) {
			return false;
		}
		if (commandBlacklist.get(guildId).containsKey(channelId)) {
			if (commandBlacklist.get(guildId).get(channelId).containsKey(commandName)) {
				return commandBlacklist.get(guildId).get(channelId).get(commandName);
			}
			return false;
		}
		if (commandBlacklist.get(guildId).containsKey("0")) {
			if (commandBlacklist.get(guildId).get("0").containsKey(commandName)) {
				return commandBlacklist.get(guildId).get("0").get(commandName);
			}
			return false;
		}
		return false;
	}

	/**
	 * @param key command with or without the Config.BOT_COMMAND_PREFIX
	 * @return instance of Command for Key or null
	 */
	public static AbstractCommand getCommand(String key) {
		if (key.startsWith(Config.BOT_COMMAND_PREFIX)) {
			key = key.substring(Config.BOT_COMMAND_PREFIX.length());
		}
		if (commands.containsKey(key)) {
			return commands.get(key);
		}
		if (commandsAlias.containsKey(key)) {
			return commandsAlias.get(key);
		}
		return null;
	}

	/**
	 * Lists the active custom commands
	 *
	 * @param guildId the internal guild id
	 * @return list of code-commands
	 */
	public static List<String> getCustomCommands(int guildId) {
		List<String> cmds = new ArrayList<>();
		cmds.addAll(customCommands.keySet());
		if (guildCommands.containsKey(guildId)) {
			cmds.addAll(guildCommands.get(guildId).keySet());
		}
		return cmds;
	}

	public static AbstractCommand[] getCommandObjects() {
		return commands.values().toArray(new AbstractCommand[commands.values().size()]);
	}

	/**
	 * Add a custom static command
	 *
	 * @param input  command
	 * @param output return
	 */
	public static void addCustomCommand(int guildId, String input, String output) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, guildId);
			WebDb.get().query("INSERT INTO commands (server,input,output) VALUES(?, ?, ?)", guildId, input, output);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		loadCustomCommands(guildId);
	}

	/**
	 * Loads all the custom commands
	 */
	private static void loadCustomCommands() {
		try (ResultSet r = WebDb.get().select("SELECT server,input, output FROM commands ")) {
			while (r != null && r.next()) {
				int guildId = r.getInt("server");
				if (guildId == 0) {
					if (!commands.containsKey(r.getString("input"))) {
						customCommands.put(r.getString("input"), r.getString("output"));
					}
				} else {
					if (!guildCommands.containsKey(guildId)) {
						guildCommands.put(guildId, new ConcurrentHashMap<>());
					}
					guildCommands.get(guildId).put(r.getString("input"), r.getString("output"));
				}
			}
			if (r != null) {
				r.getStatement().close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void loadCustomCommands(int guildId) {
		removeGuild(guildId);
		try (ResultSet r = WebDb.get().select("SELECT input, output FROM commands WHERE server = ?", guildId)) {
			while (r != null && r.next()) {
				if (!guildCommands.containsKey(guildId)) {
					guildCommands.put(guildId, new ConcurrentHashMap<>());
				}
				guildCommands.get(guildId).put(r.getString("input"), r.getString("output"));
			}

			if (r != null) {
				r.getStatement().close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Loads aliases for the commands
	 */
	private static void loadAliases() {
		for (AbstractCommand command : commands.values()) {
			for (String alias : command.getAliases()) {
				if (!commandsAlias.containsKey(alias)) {
					commandsAlias.put(alias, command);
				} else {
					DiscordBot.LOGGER.warn(String.format("Duplicate alias found! The commands `%s` and `%s` use the alias `%s`",
							command.getCommand(), commandsAlias.get(alias).getCommand(), alias));
				}
			}
		}
	}

	/**
	 * Checks if the command category is enabled or not
	 *
	 * @param category the category to check
	 * @return enabled?
	 */
	private static boolean isCommandCategoryEnabled(CommandCategory category) {
		switch (category) {
			case MUSIC:
				return Config.MODULE_ECONOMY_ENABLED;
			case ECONOMY:
				return Config.MODULE_ECONOMY_ENABLED;
			case POE:
				return Config.MODULE_POE_ENABLED;
			case HEARTHSTONE:
				return Config.MODULE_HEARTHSTONE_ENABLED;
			default:
				return true;
		}
	}

	/**
	 * Lists the active commands
	 *
	 * @return list of code-commands
	 */
	public static String[] getCommands() {
		return commands.keySet().toArray(new String[commands.keySet().size()]);
	}

	/**
	 * Initializes the commands
	 */
	public static void initialize() {
		loadCommands();
		loadAliases();
		loadCustomCommands();
		reloadBlackList();
	}

	/**
	 * removes a custom command
	 *
	 * @param guildId internal id of the guild
	 * @param input   command
	 */
	public static void removeCustomCommand(int guildId, String input) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, guildId);
			loadCustomCommands();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * initializes the commands
	 */
	private static void loadCommands() {
		Reflections reflections = new Reflections("discordbot.command");
		Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
		for (Class<? extends AbstractCommand> s : classes) {
			try {
				if (Modifier.isAbstract(s.getModifiers())) {
					continue;
				}
				String packageName = s.getPackage().getName();
				AbstractCommand c = s.getConstructor().newInstance();
				c.setCommandCategory(CommandCategory.fromPackage(packageName.substring(packageName.lastIndexOf(".") + 1)));
				if (!c.isEnabled()) {
					continue;
				}
				if (!isCommandCategoryEnabled(c.getCommandCategory())) {
					continue;
				}
				if (!commands.containsKey(c.getCommand())) {
					commands.put(c.getCommand(), c);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * (re-)loads the guild-specific blacklisted commands
	 * Map<Integer, Map<String, HashSet<String>>>
	 */
	private static void reloadBlackList() {
		commandBlacklist.clear();
		List<OBlacklistCommand> blacklisted = CBlacklistCommand.getAllBlacklisted();
		for (OBlacklistCommand item : blacklisted) {
			if (!commandBlacklist.containsKey(item.guildId)) {
				commandBlacklist.put(item.guildId, new HashMap<>());
			}
			if (!commandBlacklist.get(item.guildId).containsKey(item.channelId)) {
				commandBlacklist.get(item.guildId).put(item.channelId, new HashMap<>());
			}
			commandBlacklist.get(item.guildId).get(item.channelId).put(item.command, item.blacklisted);
		}
	}

	/**
	 * reloads the blacklist for a guild
	 *
	 * @param guildId internal guildid to reload it for
	 */
	public synchronized static void reloadBlackListFor(int guildId) {
		if (commandBlacklist.containsKey(guildId)) {
			commandBlacklist.get(guildId).clear();
		} else {
			commandBlacklist.put(guildId, new HashMap<>());
		}
		List<OBlacklistCommand> blacklisted = CBlacklistCommand.getBlacklistedFor(guildId);
		for (OBlacklistCommand item : blacklisted) {
			if (!commandBlacklist.get(item.guildId).containsKey(item.channelId)) {
				commandBlacklist.get(item.guildId).put(item.channelId, new HashMap<>());
			}
			commandBlacklist.get(item.guildId).get(item.channelId).put(item.command, item.blacklisted);
		}
	}
}