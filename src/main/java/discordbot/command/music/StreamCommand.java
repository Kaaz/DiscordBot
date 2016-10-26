package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * Stream from url
 */
public class StreamCommand extends AbstractCommand {
	public StreamCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Use a stream as input for the music source";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String getCommand() {
		return "stream";
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
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		TextChannel tc = (TextChannel) channel;
		if (!tc.getGuild().getAudioManager().isConnected()) {
			return Template.get("music_no_users_in_channel");
		}
		bot.addStreamToQueue(args[0], tc.getGuild());
		return Template.get("music_streaming_from_url");
	}
}