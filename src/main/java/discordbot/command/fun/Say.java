package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !say
 * make the bot say something
 */
public class Say extends AbstractCommand {
	public Say(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "repeats you";
	}

	@Override
	public String getCommand() {
		return "say";
	}

	@Override
	public String[] getUsage() {
		return new String[]{"say <anything>"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
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
			return Template.get("command_say_whatexactly");
		}
	}
}