package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !config
 * gets/sets the configuration of the bot
 */
public class SetConfig extends AbstractCommand {
	public SetConfig(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Gets/sets the configuration of the bot";
	}

	@Override
	public String getCommand() {
		return "config";
	}

	@Override
	public String getUsage() {
		return "config <set|get> <property> <value>";
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		boolean first = true;
		String ret = "";
		return TextHandler.get("command_say_whatexactly");
	}
}