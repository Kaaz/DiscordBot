package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !stop
 * make the bot stop playing music
 */
public class Stop extends AbstractCommand {
	public Stop(DiscordBot b) {
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
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, TextChannel channel, User author) {
		bot.stopMusic(channel.getGuild());
		return Template.get("command_stop_success");
	}
}