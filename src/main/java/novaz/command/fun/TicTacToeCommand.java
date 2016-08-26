package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class TicTacToeCommand extends AbstractCommand {
	public TicTacToeCommand(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Play a game of tic tac toe with someone or with me!";
	}

	@Override
	public String getCommand() {
		return "tic";
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
		return TextHandler.get("command_not_implemented");
	}
}