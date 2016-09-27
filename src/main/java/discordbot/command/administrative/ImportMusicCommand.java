package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created on 27-9-2016
 */
public class ImportMusicCommand extends AbstractCommand {
	public ImportMusicCommand(DiscordBot bot) {
		super(bot);
	}

	@Override
	public String getDescription() {
		return "import msc";
	}

	@Override
	public String getCommand() {
		return "importmusic";
	}

	@Override
	public String[] getUsage() {
		return new String[0];
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		return "notyet";
	}
}
