package novaz.handler;

import novaz.command.CommandCategory;
import novaz.core.AbstractCommand;
import novaz.db.WebDb;
import novaz.db.table.TCommandLog;
import novaz.db.table.TServers;
import novaz.db.table.TUser;
import novaz.handler.guildsettings.defaults.SettingBotChannel;
import novaz.handler.guildsettings.defaults.SettingCleanupMessages;
import novaz.handler.guildsettings.defaults.SettingCommandPrefix;
import novaz.handler.guildsettings.defaults.SettingShowUnknownCommands;
import novaz.main.Config;
import novaz.main.NovaBot;
import org.reflections.Reflections;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
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
	private HashMap<String, AbstractCommand> chatCommands;
	private HashMap<String, String> customCommands;

	public CommandHandler() {
	}

	public CommandHandler(NovaBot b) {
		bot = b;
	}

	public static String filterPrefix(String command, IGuild guild) {
		String prefix = GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class);
		if (command.startsWith(prefix)) {
			command = command.substring(prefix.length());
		}
		return command;
	}

	/**
	 * directs the command to the right class
	 *
	 * @param guild   which server
	 * @param channel which channel
	 * @param author  author
	 * @param content message
	 */
	public void process(IGuild guild, IChannel channel, IUser author, IMessage content) {
		IMessage mymsg = null;
		String inputMessage = content.getContent();
		if (inputMessage.startsWith(bot.mentionMe)) {
			inputMessage = inputMessage.replace(bot.mentionMe, "").trim();
		}
		String[] input = inputMessage.split(" ");
		String args[] = new String[input.length - 1];
		input[0] = filterPrefix(input[0], guild).toLowerCase();
		System.arraycopy(input, 1, args, 0, input.length - 1);
		if (chatCommands.containsKey(input[0])) {
			String commandOutput = chatCommands.get(input[0]).execute(args, channel, author);
			if (!commandOutput.isEmpty()) {
				mymsg = bot.sendMessage(channel, commandOutput);
			}
			if (Config.BOT_COMMAND_LOGGING) {
				StringBuilder usedArguments = new StringBuilder();
				for (String arg : args) {
					usedArguments.append(arg).append(" ");
				}
				TCommandLog.saveLog(TUser.getCachedId(author.getID()), TServers.getCachedId(guild.getID()), input[0], usedArguments.toString().trim());
			}
		} else if (customCommands.containsKey(input[0])) {
			mymsg = bot.sendMessage(channel, customCommands.get(input[0]));
		} else if (Config.BOT_COMMAND_SHOW_UNKNOWN ||
				GuildSettings.get(guild).getOrDefault(SettingShowUnknownCommands.class).equals("true")) {
			mymsg = bot.sendMessage(channel, String.format(TextHandler.get("unknown_command"), GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class) + "help"));
		}
		if (mymsg != null && shouldCleanUpMessages(guild, channel)) {
			final IMessage finalMymsg = mymsg;
			bot.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						finalMymsg.delete();
						content.delete();
					} catch (MissingPermissionsException | RateLimitException | DiscordException ignored) {
					}
				}
			}, Config.DELETE_MESSAGES_AFTER);
		}
	}

	private boolean shouldCleanUpMessages(IGuild guild, IChannel channel) {
		String cleanupMethod = GuildSettings.get(guild).getOrDefault(SettingCleanupMessages.class);
		String mychannel = GuildSettings.get(guild).getOrDefault(SettingBotChannel.class);
		if (cleanupMethod.equals("yes")) {
			return true;
		} else if (cleanupMethod.equals("nonstandard") && !channel.getName().equalsIgnoreCase(mychannel)) {
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
		if (chatCommands.containsKey(key)) {
			return chatCommands.get(key);
		}
		return null;
	}

	/**
	 * Lists the active commands
	 *
	 * @return list of code-commands
	 */
	public String[] getCommands() {
		return chatCommands.keySet().toArray(new String[chatCommands.keySet().size()]);
	}

	public AbstractCommand[] getCommandObjects() {
		return chatCommands.values().toArray(new AbstractCommand[chatCommands.values().size()]);
	}

	public void load() {
		loadCommands();
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
		chatCommands = new HashMap<>();
		Reflections reflections = new Reflections("novaz.command");
		Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
		for (Class<? extends AbstractCommand> s : classes) {
			try {
				String packageName = s.getPackage().getName();
				AbstractCommand c = s.getConstructor(NovaBot.class).newInstance(bot);
				c.setCommandCategory(CommandCategory.fromPackage(packageName.substring(packageName.lastIndexOf(".") + 1)));
				if (c.getCommandCategory().equals(CommandCategory.MUSIC) && !Config.MODULE_MUSIC_ENABLED) {
					continue;
				}
				if (c.getCommandCategory().equals(CommandCategory.ECONOMY) && !Config.MODULE_ECONOMY_ENABLED) {
					continue;
				}
				if (c.getCommandCategory().equals(CommandCategory.POE) && !Config.MODULE_POE_ENABLED) {
					continue;
				}

				if (!chatCommands.containsKey(c.getCommand())) {
					chatCommands.put(c.getCommand(), c);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
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
				if (!chatCommands.containsKey(r.getString("input")) && !customCommands.containsKey(r.getString("input"))) {
					customCommands.put(r.getString("input"), r.getString("output"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}