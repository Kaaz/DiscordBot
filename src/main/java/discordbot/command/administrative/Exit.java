package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !exit
 * completely stops the program
 */
public class Exit extends AbstractCommand {
	public Exit(DiscordBot b) {
		super(b);
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
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.isCreator(author)) {
			bot.out.sendMessage(channel, "I am being killed :sob: farewell world! :wave: ");
			System.exit(0);
		}
		return Template.get("command_no_permission");
	}
}