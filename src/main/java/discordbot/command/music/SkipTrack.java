package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !skip
 * skips current active track
 */
public class SkipTrack extends AbstractCommand {
	public SkipTrack(DiscordBot b) {
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
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
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
					return Template.get("command_skip_permanent_success");
				default:
					return Template.get("command_invalid_usage");
			}

		}
		bot.skipCurrentSong(channel.getGuild());
		return Template.get("command_skip_song_skipped");
	}
}
