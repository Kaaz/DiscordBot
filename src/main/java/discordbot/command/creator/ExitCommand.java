package discordbot.command.creator;

import discordbot.core.AbstractCommand;
import discordbot.core.ExitCode;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !exit
 * completely stops the program
 */
public class ExitCommand extends AbstractCommand {
	public ExitCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "completely shuts the bot down";
	}

	@Override
	public String getCommand() {
		return "exit";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"brexit"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);
		if (rank.isAtLeast(SimpleRank.CREATOR)) {
			bot.out.sendAsyncMessage(channel, "I am being killed :sob: farewell world! :wave: ", message -> {
				Launcher.stop(ExitCode.STOP);
			});
		}
		return Template.get("command_no_permission");
	}
}