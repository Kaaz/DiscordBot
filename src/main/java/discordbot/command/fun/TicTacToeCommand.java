package discordbot.command.fun;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class TicTacToeCommand extends AbstractCommand {
	public TicTacToeCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Deprecated, see game";
	}

	@Override
	public String getCommand() {
		return "tic";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				""};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		return "The games have been moved to the `" + DisUtil.getCommandPrefix(channel) + "game` command";
	}
}