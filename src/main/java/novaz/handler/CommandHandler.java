package novaz.handler;

import novaz.core.AbstractCommand;
import novaz.main.NovaBot;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

/**
 * Handles all the commands
 */
public class CommandHandler {

	public final String commandPrefix = "!";
	private NovaBot bot;
	private HashMap<String, AbstractCommand> chatCommands;
	private HashMap<String, String> customCommands;

	public CommandHandler(NovaBot b) {
		bot = b;
	}

	public void process(String sender, String message, boolean userIsOp) {
		String[] input = message.split(" ");
		String args[] = new String[input.length - 1];
		for (int i = 1; i < input.length; i++) {
			args[i - 1] = input[i];
		}
		if (chatCommands.containsKey(input[0])) {
//			bot.msg(chatCommands.get(input[0]).execute(args, sender, userIsOp));
		} else if (customCommands.containsKey(input[0])) {
//			bot.msg(customCommands.get(input[0]));
		} else {
//			bot.msg(TextHandler.get("unknown_command"));
		}
	}

	public void load() {
		loadCommands();
		loadCustomCommands();
	}

//	public String[] getCommands() {
//		return MapUtil.mapKeysToArray(chatCommands);
//	}

//	public String[] getCustomCommands() {
//		return MapUtil.mapKeysToArray(customCommands);
//	}

//		public void addCustomCommand (String input, String output){
//		log.info("adding the command " + input);
//		Db.query("DELETE FROM command WHERE input = ? AND channel = ?", input, bot.defaultChannel);
//		Db.query("INSERT INTO command (channel,input,output) VALUES(?, ?, ?)", bot.defaultChannel, input, output);
//			loadCustomCommands();
//		}

//	public void removeCustomCommand(String input) {
//		log.info("Deleting the command: " + input);
//		Db.query("DELETE FROM command WHERE input = ? AND channel = ?", input, bot.defaultChannel);
//		loadCustomCommands();
//	}

	private void loadCommands() {
		chatCommands = new HashMap<>();
		Reflections reflections = new Reflections("novaz.command");
		Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
		for (Class<? extends AbstractCommand> s : classes) {
			try {
				AbstractCommand c = s.getConstructor(NovaBot.class).newInstance(bot);
				if (!chatCommands.containsKey(commandPrefix + c.getCmd())) {
					chatCommands.put(commandPrefix + c.getCmd(), c);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadCustomCommands() {
		customCommands = new HashMap<>();
//		try (ResultSet r = Db.select("SELECT input, output FROM command WHERE channel = ? ", bot.defaultChannel)) {
//			while (r != null && r.next()) {
//				if (!chatCommands.containsKey(commandPrefix + r.getString("input")) && !customCommands.containsKey(commandPrefix + r.getString("input"))) {
//					customCommands.put(commandPrefix + r.getString("input"), r.getString("output"));
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}

	}
}