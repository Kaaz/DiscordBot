package novaz.command.music;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !skip
 * skips current active track
 */
public class SkipTrack extends AbstractCommand {
	public SkipTrack(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "skip current track";
	}

	@Override
	public String getCommand() {
		return "skip";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		bot.skipCurrentSong(channel.getGuild());
		return TextHandler.get("command_skip_song_skipped");
	}
}
