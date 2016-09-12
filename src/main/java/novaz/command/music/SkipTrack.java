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
		return new String[]{
				"skip      //skips current track",
				"skip perm //skips permanently; never hear this song again"
		};
	}

	@Override
	public boolean isAllowedInPrivateChannel() {
		return false;
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length >= 1) {
			switch (args[0]) {
				case "perm":
				case "permanent":
					return TextHandler.get("command_skip_permanent_success");
				default:
					return TextHandler.get("command_invalid_usage");
			}

		}
		bot.skipCurrentSong(channel.getGuild());
		return TextHandler.get("command_skip_song_skipped");
	}
}
