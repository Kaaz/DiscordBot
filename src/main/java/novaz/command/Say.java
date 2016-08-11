package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !say
 * make the bot say something
 */
public class Say extends AbstractCommand {
	public Say(NovaBot b) {
		super(b);
		setCmd("say");
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		boolean first = true;
		String ret = "";
		if (args.length > 0) {
			for (String s : args) {
				if (first) {
					first = false;
					ret += s;
				} else {
					ret += " " + s;
				}

			}
			return ret;
		} else {
			return TextHandler.get("command_say_whatexactly");
		}
	}
}
