package novaz.command.music;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !stop
 * make the bot stop playing music
 */
public class Stop extends AbstractCommand {
	public Stop(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "stops playing music";
	}

	@Override
	public String getCommand() {
		return "stop";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		bot.stopMusic(channel.getGuild());
		return TextHandler.get("command_stop_success");
	}
}