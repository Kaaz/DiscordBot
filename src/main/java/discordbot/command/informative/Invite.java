package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !invite
 * Instructions on how to invite the bot to a discord server
 */
public class Invite extends AbstractCommand {
	public Invite(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, IChannel channel, IUser author) {
		return "I am honored you'd want to invite me! :hugging: " + Config.EOL +
				"You can add me to your guild/server with the following link : " + Config.EOL +
				"https://discordapp.com/oauth2/authorize?client_id=" + bot.instance.getOurUser().getID() + "&scope=bot";
	}
}