package discordbot.handler;

import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandCategory;
import discordbot.command.CommandVisibility;
import discordbot.command.ICommandCooldown;
import discordbot.core.AbstractCommand;
import discordbot.db.WebDb;
import discordbot.db.model.OCommandCooldown;
import discordbot.db.table.TCommandCooldown;
import discordbot.db.table.TCommandLog;
import discordbot.db.table.TServers;
import discordbot.db.table.TUser;
import discordbot.guildsettings.defaults.*;
import discordbot.main.Config;
import discordbot.main.NovaBot;
import discordbot.util.DisUtil;
import discordbot.util.TimeUtil;
import org.reflections.Reflections;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import java.util.TimerTask;

/**
 * Handles all the commands
 */
public class CommandHandler {

	private NovaBot bot;
	private HashMap<String, AbstractCommand> commands;
	private HashMap<String, AbstractCommand> commandsAlias;
	private HashMap<String, String> customCommands;

	public CommandHandler() {
	}

	public CommandHandler(NovaBot b) {
		bot = b;
	}

	/**
	 * checks if the the message in channel is a command
	 *
	 * @param channel the channel the message came from
	 * @param msg     the message
	 * @return whether or not the message is a command
	 */
	public boolean isCommand(IChannel channel, String msg) {
		return msg.startsWith(DisUtil.getCommandPrefix(channel)) || msg.startsWith(bot.mentionMe);
	}

	/**
	 * directs the command to the right class
	 *
	 * @param channel          which channel
	 * @param author           author
	 * @param incommingMessage message
	 */
	public void process(IChannel channel, IUser author, String incommingMessage) {
		IMessage mymsg = null;
		boolean startedWithMention = false;
		String inputMessage = incommingMessage;
		if (inputMessage.startsWith(bot.mentionMe)) {
			inputMessage = inputMessage.replace(bot.mentionMe, "").trim();
			startedWithMention = true;
		}
		String[] input = inputMessage.split(" ");
		String args[] = new String[input.length - 1];
		input[0] = DisUtil.filterPrefix(input[0], channel).toLowerCase();
		System.arraycopy(input, 1, args, 0, input.length - 1);
		if (commands.containsKey(input[0]) || commandsAlias.containsKey(input[0])) {
			AbstractCommand command = commands.containsKey(input[0]) ? commands.get(input[0]) : commandsAlias.get(input[0]);
			long cooldown = getCommandCooldown(command, author, channel);
			if (hasRightVisibility(channel, command.getVisibility()) && cooldown <= 0) {
				String commandOutput = command.execute(args, channel, author);
				if (!commandOutput.isEmpty()) {
					mymsg = bot.out.sendMessage(channel, commandOutput);
				}
				if (Config.BOT_COMMAND_LOGGING) {
					StringBuilder usedArguments = new StringBuilder();
					for (String arg : args) {
						usedArguments.append(arg).append(" ");
					}
					if (!channel.isPrivate()) {
						TCommandLog.saveLog(TUser.getCachedId(author.getID()), TServers.getCachedId(channel.getGuild().getID()), input[0], EmojiParser.parseToAliases(usedArguments.toString()).trim());
					}
				}
			} else if (cooldown > 0) {
				mymsg = bot.out.sendMessage(channel, String.format(TextHandler.get("command_on_cooldown"), TimeUtil.getRelativeTime((System.currentTimeMillis() / 1000L) + cooldown, false)));
			} else if (!hasRightVisibility(channel, command.getVisibility())) {
				if (channel instanceof IPrivateChannel) {
					mymsg = bot.out.sendMessage(channel, TextHandler.get("command_not_for_private"));
				} else {
					mymsg = bot.out.sendMessage(channel, TextHandler.get("command_not_for_public"));
				}
			}
		} else if (customCommands.containsKey(input[0])) {
			mymsg = bot.out.sendMessage(channel, customCommands.get(input[0]));
		} else if (startedWithMention && Config.BOT_CHATTING_ENABLED && GuildSettings.getFor(channel, SettingEnableChatBot.class).equals("true")) {
			mymsg = bot.out.sendMessage(channel, author.mention() + ", " + bot.chatBotHandler.chat(inputMessage));
		} else if (Config.BOT_COMMAND_SHOW_UNKNOWN ||
				GuildSettings.getFor(channel, SettingShowUnknownCommands.class).equals("true")) {
			mymsg = bot.out.sendMessage(channel, String.format(TextHandler.get("unknown_command"), GuildSettings.getFor(channel, SettingCommandPrefix.class) + "help"));
		}
		if (mymsg != null && shouldCleanUpMessages(channel)) {
			final IMessage finalMymsg = mymsg;
			bot.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						finalMymsg.delete();
					} catch (MissingPermissionsException | RateLimitException | DiscordException ignored) {
					}
				}
			}, Config.DELETE_MESSAGES_AFTER);
		}
	}

	private boolean hasRightVisibility(IChannel channel, CommandVisibility visibility) {
		if (channel instanceof IPrivateChannel) {
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
	private long getCommandCooldown(AbstractCommand command, IUser author, IChannel channel) {
		if (command instanceof ICommandCooldown) {
			long now = System.currentTimeMillis() / 1000L;
			ICommandCooldown cd = (ICommandCooldown) command;
			String targetId;
			switch (cd.getCooldownScale()) {
				case USER:
					targetId = author.getID();
					break;
				case CHANNEL:
					targetId = channel.getID();
					break;
				case GUILD:
					if (channel.isPrivate()) {
						bot.out.sendErrorToMe(new Exception("Command with guild-scale cooldown in private!"), "command", command.getCommand(), "user", author.getName(), bot);
					}
					targetId = channel.getGuild().getID();
					break;
				case GLOBAL:
					targetId = "GLOBAL";
					break;
				default:
					targetId = "";
					break;
			}
			OCommandCooldown cooldown = TCommandCooldown.findBy(command.getCommand(), targetId, cd.getCooldownScale().getId());
			if (cooldown.lastTime + cd.getCooldownDuration() <= now) {

				cooldown.command = command.getCommand();
				cooldown.targetId = targetId;
				cooldown.targetType = cd.getCooldownScale().getId();
				cooldown.lastTime = now;
				TCommandCooldown.insertOrUpdate(cooldown);
				return 0;
			}
			return cooldown.lastTime + cd.getCooldownDuration() - now;
		}
		return 0;
	}

	private boolean shouldCleanUpMessages(IChannel channel) {
		String cleanupMethod = GuildSettings.getFor(channel, SettingCleanupMessages.class);
		String mychannel = GuildSettings.getFor(channel, SettingBotChannel.class);
		if ("yes".equals(cleanupMethod)) {
			return true;
		} else if ("nonstandard".equals(cleanupMethod) && !channel.getName().equalsIgnoreCase(mychannel)) {
			return true;
		}
		return false;
	}

	/**
	 * @param key command with or without the Config.BOT_COMMAND_PREFIX
	 * @return instance of Command for Key or null
	 */
	public AbstractCommand getCommand(String key) {
		if (key.startsWith(Config.BOT_COMMAND_PREFIX)) {
			key = key.substring(Config.BOT_COMMAND_PREFIX.length());
		}
		if (commands.containsKey(key)) {
			return commands.get(key);
		}
		return null;
	}

	/**
	 * Lists the active commands
	 *
	 * @return list of code-commands
	 */
	public String[] getCommands() {
		return commands.keySet().toArray(new String[commands.keySet().size()]);
	}

	/**
	 * Lists the active custom commands
	 *
	 * @return list of code-commands
	 */
	public String[] getCustomCommands() {
		return customCommands.keySet().toArray(new String[customCommands.keySet().size()]);
	}

	public AbstractCommand[] getCommandObjects() {
		return commands.values().toArray(new AbstractCommand[commands.values().size()]);
	}

	public void load() {
		loadCommands();
		loadAliases();
		loadCustomCommands();
	}

	/**
	 * Add a custom static command
	 *
	 * @param input  command
	 * @param output return
	 */
	public void addCustomCommand(String input, String output) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = 1", input);
			WebDb.get().query("INSERT INTO commands (server,input,output) VALUES(1, ?, ?)", input, output);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		loadCustomCommands();
	}

	/**
	 * removes a custom command
	 *
	 * @param input command
	 */
	public void removeCustomCommand(String input) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ?", input);
			loadCustomCommands();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * initializes the commands
	 */
	private void loadCommands() {
		commands = new HashMap<>();
		Reflections reflections = new Reflections("discordbot.command");
		Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
		for (Class<? extends AbstractCommand> s : classes) {
			try {
				String packageName = s.getPackage().getName();
				AbstractCommand c = s.getConstructor(NovaBot.class).newInstance(bot);
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
	 * Checks if the command category is enabled or not
	 *
	 * @param category the category to check
	 * @return enabled?
	 */
	private boolean isCommandCategoryEnabled(CommandCategory category) {
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
	 * Loads aliases for the commands
	 */
	private void loadAliases() {
		commandsAlias = new HashMap<>();
		for (AbstractCommand command : commands.values()) {
			for (String alias : command.getAliases()) {
				if (!commandsAlias.containsKey(alias)) {
					commandsAlias.put(alias, command);
				} else {
					NovaBot.LOGGER.warn("Duplicate alias found! The commands `%s` and `%s` use the alias `%s`",
							command.getCommand(), commandsAlias.get(alias).getCommand(), alias);
				}
			}
		}
	}

	/**
	 * Loads all the custom commands
	 */
	private void loadCustomCommands() {
		customCommands = new HashMap<>();
		try (ResultSet r = WebDb.get().select("SELECT input, output FROM commands ")) {
			while (r != null && r.next()) {
				if (!commands.containsKey(r.getString("input")) && !customCommands.containsKey(r.getString("input"))) {
					customCommands.put(r.getString("input"), r.getString("output"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}