package novaz.command.informative;

import com.google.common.base.Joiner;
import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import novaz.modules.pathofexile.ItemAnalyzer;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !poeitem
 * Analyzes an item from path of exile
 */
public class PoeItemCommand extends AbstractCommand {
	public PoeItemCommand(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Analyzes an item from path of exile.";
	}

	@Override
	public String getCommand() {
		return "poeitem";
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
		String input = Joiner.on(" ").join(args);
		ItemAnalyzer itemAnalyzer = new ItemAnalyzer();
		return itemAnalyzer.attemptToANALyze(input).toString();

//		return TextHandler.get("command_not_implemented");
	}
}