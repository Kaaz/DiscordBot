package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !volume [vol]
 * sets the volume of the music player
 * With no params returns the current volume
 */
public class Volume extends AbstractCommand {
	public Volume(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "gets and sets the volume of the music";
	}

	@Override
	public String getCommand() {
		return "volume";
	}

	@Override
	public String getUsage() {
		return "volume or volume <1 to 100>";
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			float volume;
			try {
				volume = Float.parseFloat(args[0]);
				if (volume > 0 && volume <= 100) {
					bot.setVolume(channel.getGuild(), volume / 100F);
					return TextHandler.get("command_volume_changed");
				}
			} catch (NumberFormatException ignored) {
			}
			return TextHandler.get("command_volume_invalid_parameters");
		}
		return "Current volume: " + bot.getVolume(channel.getGuild()) * 100 + "%";
	}
}
