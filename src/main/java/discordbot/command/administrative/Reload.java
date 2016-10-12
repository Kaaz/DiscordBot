package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !reload
 * reloads config
 */
public class Reload extends AbstractCommand {
	public Reload(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "reloads the configuration";
	}

	@Override
	public String getCommand() {
		return "reload";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, TextChannel channel, User author) {
		if (!bot.isAdmin(channel, author)) {
			return Template.get("no_permission");
		}
		bot.loadConfiguration();
		return Template.get("command_reload_success");
	}
}