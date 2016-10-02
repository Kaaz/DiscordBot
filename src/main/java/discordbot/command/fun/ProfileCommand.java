package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created on 2016-09-24.
 */
public class ProfileCommand extends AbstractCommand {
	public ProfileCommand(DiscordBot bot) {
		super(bot);
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getCommand() {
		return null;
	}

	@Override
	public String[] getUsage() {
		return new String[0];
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		return null;
	}
}
