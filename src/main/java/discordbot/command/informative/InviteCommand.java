package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !invite
 * Instructions on how to invite the bot to a discord server
 */
public class InviteCommand extends AbstractCommand {
	public InviteCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Provides an invite link to add the bot to your server.";
	}

	@Override
	public String getCommand() {
		return "invite";
	}

	@Override
	public boolean isBlacklistable() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"inv"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		return "I am honored you'd want to invite me! :hugging: " + Config.EOL +
				"You can add me to your guild/server with the following link : " + Config.EOL +
				"https://discordapp.com/oauth2/authorize?client_id=" + bot.client.getSelfInfo().getId() + "&scope=bot&permissions=70634560";
	}
}