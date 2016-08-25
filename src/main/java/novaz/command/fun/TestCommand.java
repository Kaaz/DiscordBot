package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.games.card.Card;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Collections;

/**
 * !test
 * testing things
 */
public class TestCommand extends AbstractCommand {
	public TestCommand(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "no one truly knows";
	}

	@Override
	public String getCommand() {
		return "test";
	}

	@Override
	public String[] getUsage() {
		return new String[]{"lorem ipsum dolar sit amet"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isCreator(author)) {
			return "";
		}
		ArrayList<Card> cards = Card.newDeck();
		String ret = "shuffled cards: " + Config.EOL;
		Collections.shuffle(cards);
		int cardsPerLine = cards.size() / 4;
		int index = 0;
		for (Card c : cards) {
			index++;
			ret += " [" + c.toEmote() + "] ";
			if (index % cardsPerLine == 0) {
				ret += Config.EOL;
			}
		}
		return ret;
	}
}