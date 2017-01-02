package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class ChangelogCommand extends AbstractCommand {
	public ChangelogCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Check out whats new";
	}

	@Override
	public String getCommand() {
		return "changelog";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"changelog               //shows changes for the latest version",
				"changelog <version>     //shows changes for that version",
				"",
				"example:",
				"changelog 1.9.6",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		return ":eyes:";
	}
}