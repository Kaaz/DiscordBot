package novaz.handler;

import novaz.core.AbstractCommand;
import novaz.db.WebDb;
import novaz.handler.guildsettings.defaults.SettingBotChannel;
import novaz.handler.guildsettings.defaults.SettingCleanupMessages;
import novaz.handler.guildsettings.defaults.SettingCommandPrefix;
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
		IMessage mymsg;
		String[] input = content.getContent().split(" ");
		String args[] = new String[input.length - 1];
		input[0] = filterPrefix(input[0], guild).toLowerCase();
		System.arraycopy(input, 1, args, 0, input.length - 1);
		if (chatCommands.containsKey(input[0])) {
			mymsg = bot.sendMessage(channel, chatCommands.get(input[0]).execute(args, channel, author));
		} else if (customCommands.containsKey(input[0])) {
			mymsg = bot.sendMessage(channel, customCommands.get(input[0]));
		} else {
			mymsg = bot.sendMessage(channel, TextHandler.get("unknown_command"));
		}
		if (shouldCleanUpMessages(guild, channel)) {
			bot.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						mymsg.delete();
						content.delete();
					} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
//					e.printStackTrace();
					}
				}
			}, Config.DELETE_MESSAGES_AFTER);
		}
	}

	public boolean shouldCleanUpMessages(IGuild guild, IChannel channel) {
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

	public void load() {
		loadCommands();
		loadCustomCommands(1);
	}

	/**
	 * Add a custom static command
	 *
	 * @param serverId id of server
	 * @param input    command
	 * @param output   return
	 */
	public void addCustomCommand(int serverId, String input, String output) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, serverId);
			WebDb.get().query("INSERT INTO commands (server,input,output) VALUES(?, ?, ?)", serverId, input, output);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		loadCustomCommands(serverId);
	}

	/**
	 * removes a custom command
	 *
	 * @param serverId id of server
	 * @param input    command
	 */
	public void removeCustomCommand(int serverId, String input) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, serverId);
			loadCustomCommands(serverId);
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
				AbstractCommand c = s.getConstructor(NovaBot.class).newInstance(bot);
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
	 *
	 * @param serverId id of server
	 */
	private void loadCustomCommands(int serverId) {
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