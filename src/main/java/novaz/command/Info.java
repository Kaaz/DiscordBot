package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !leave
 * make the bot leave
 */
public class Info extends AbstractCommand {
	public Info(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Shows info about the bot";
	}

	@Override
	public String getCommand() {
		return "info";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		return TextHandler.get("command_not_implemented");
	}
}