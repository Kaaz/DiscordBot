package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !skip
 * skips current active track
 */
public class SkipTrack extends AbstractCommand {
	public SkipTrack() {
		super();
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
		return new String[]{
				"next"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
		if (!player.isPlaying()) {

		}
		if (args.length >= 1) {
			switch (args[0]) {
				case "perm":
				case "permanent":
					return Template.get("command_skip_permanent_success");
				default:
					return Template.get("command_invalid_usage");
			}

		}
		player.skipSong();
		return "";
	}
}
