package novaz.command.administrative;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !subscribe
 * subscripe to certain events
 */
public class Subscribe extends AbstractCommand {
	public Subscribe(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "subscribe the channel to certain events";
	}

	@Override
	public String getCommand() {
		return "subscribe";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"subscribe                //check what subscriptions are active",
				"subscribe <subject>      //subscribe to subject",
				"subscribe stop <subject> //stop subscription to subject",
				"subscribe list           //See what subscription options there are",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {

		return TextHandler.get("command_not_implemented");
	}
}