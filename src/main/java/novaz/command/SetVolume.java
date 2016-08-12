package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !setvolume <vol>
 * sets the volume of the music player
 */
public class SetVolume extends AbstractCommand {
	public SetVolume(NovaBot b) {
		super(b);
		setCmd("setvolume");
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			float volume;
			try {
				volume = Float.parseFloat(args[1]);
				if (volume > 0 && volume < 100) {
					bot.setVolume(channel.getGuild(), volume / 100F);
					return TextHandler.get("command_setvolume_changed");
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return TextHandler.get("command_setvolume_invalid_parameters");
	}
}
