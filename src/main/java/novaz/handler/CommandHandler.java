package novaz.handler;

import novaz.core.AbstractCommand;
import novaz.db.WebDb;
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

	public CommandHandler(NovaBot b) {
		bot = b;
	}

	public void process(IGuild guild, IChannel channel, IUser author, IMessage content) {
		IMessage mymsg;
		String[] input = content.getContent().split(" ");
		String args[] = new String[input.length - 1];
		for (int i = 1; i < input.length; i++) {
			args[i - 1] = input[i];
		}
		if (chatCommands.containsKey(input[0])) {
			mymsg = bot.sendMessage(channel, chatCommands.get(input[0]).execute(args, channel, author));
		} else if (customCommands.containsKey(input[0])) {
			mymsg = bot.sendMessage(channel, customCommands.get(input[0]));
		} else {
			mymsg = bot.sendMessage(channel, TextHandler.get("unknown_command"));
		}
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

	public AbstractCommand getCommand(String key) {
		if (chatCommands.containsKey(Config.BOT_COMMAND_PREFIX + key)) {
			return chatCommands.get(Config.BOT_COMMAND_PREFIX + key);
		}
		return null;
	}

	public String[] getCommands() {
		return chatCommands.keySet().toArray(new String[chatCommands.keySet().size()]);
	}

	public void load() {
		loadCommands();
		loadCustomCommands(1);
	}

	public void addCustomCommand(int serverId, String input, String output) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, serverId);
			WebDb.get().query("INSERT INTO commands (server,input,output) VALUES(?, ?, ?)", serverId, input, output);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		loadCustomCommands(serverId);
	}

	public void removeCustomCommand(int serverId, String input) {
		try {
			WebDb.get().query("DELETE FROM commands WHERE input = ? AND server = ?", input, serverId);
			loadCustomCommands(serverId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadCommands() {
		chatCommands = new HashMap<>();
		Reflections reflections = new Reflections("novaz.command");
		Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
		for (Class<? extends AbstractCommand> s : classes) {
			try {
				AbstractCommand c = s.getConstructor(NovaBot.class).newInstance(bot);
				if (!chatCommands.containsKey(Config.BOT_COMMAND_PREFIX + c.getCmd())) {
					chatCommands.put(Config.BOT_COMMAND_PREFIX + c.getCmd(), c);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadCustomCommands(int serverId) {
		customCommands = new HashMap<>();
		try (ResultSet r = WebDb.get().select("SELECT input, output FROM commands ")) {
//		try (ResultSet r = WebDb.get().select("SELECT input, output FROM commands WHERE server = ? ", serverId)) {
			while (r != null && r.next()) {
				if (!chatCommands.containsKey(Config.BOT_COMMAND_PREFIX + r.getString("input")) && !customCommands.containsKey(Config.BOT_COMMAND_PREFIX + r.getString("input"))) {
					customCommands.put(Config.BOT_COMMAND_PREFIX + r.getString("input"), r.getString("output"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}