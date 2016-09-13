package novaz.command.informative;

import novaz.command.CooldownScale;
import novaz.command.ICommandCooldown;
import novaz.core.AbstractCommand;
import novaz.handler.CommandHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.TimeUtil;
import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;


/**
 * !leave
 * make the bot leave
 */
public class Info extends AbstractCommand implements ICommandCooldown {
	private Trello trello;

	public Info(NovaBot b) {
		super(b);
		trello = new TrelloImpl(Config.TRELLO_API_KEY, Config.TRELLO_TOKEN);
	}

	@Override
	public long getCooldown() {
		return 15L;
	}

	@Override
	public CooldownScale getCooldownScale() {
		return CooldownScale.CHANNEL;
	}

	@Override
	public String getDescription() {
		return "Shows info about the bot";
	}

	@Override
	public String getCommand() {
		return "info";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"info          //general info",
				"info planned  //see whats planned in the near future",
				"info bugs     //known bugs",
				"info progress //see whats currently being worked on",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0 && Config.TRELLO_ACTIVE) {
			switch (args[0].toLowerCase()) {
				case "planned":
				case "plan":
					return "The following items are planned:" + Config.EOL + getListFor(Config.TRELLO_LIST_PLANNED, ":date:");
				case "bugs":
				case "bug":
					return "The following bugs are known:" + Config.EOL + getListFor(Config.TRELLO_LIST_BUGS, ":exclamation:");
				case "progress":
					return "The following items are being worked on:" + Config.EOL + getListFor(Config.TRELLO_LIST_IN_PROGRESS, ":construction:");
			}
		}
		String onlineFor = TimeUtil.getRelativeTime(bot.startupTimeStamp, false);
		IUser user = bot.instance.getUserByID(Config.CREATOR_ID);
		String response = bot.chatBotHandler.chat("What are you?");
		if (response.isEmpty()) {
			response = "I'm batman";
		}
		return "What am I? *" + response + "* " + Config.EOL +
				"Currently active on " + bot.instance.getGuilds().size() + " guilds and the last time I restarted was  " + onlineFor + "." + Config.EOL +
				"There are " + bot.commandHandler.getCommands().length + " commands I can perform type **" + CommandHandler.getCommandPrefix(channel) + "help** for a full list" + Config.EOL +
				"If I can't help you out, you can always try to poke __" + user.getName() + "#" + user.getDiscriminator() + "__";
	}

	private String getListFor(String listId, String itemPrefix) {
		StringBuilder sb = new StringBuilder();
		List<Card> cardsByList = trello.getCardsByList(listId);
		for (Card card : cardsByList) {
			sb.append(itemPrefix).append(" **").append(card.getName()).append("**").append(Config.EOL);
			if (card.getDesc().length() > 2) {
				sb.append(card.getDesc()).append(Config.EOL);
			}
			List<Checklist> checkItemStates = trello.getChecklistByCard(card.getId());
			for (Checklist clist : checkItemStates) {
				sb.append(Config.EOL);
				for (Checklist.CheckItem item : clist.getCheckItems()) {
					sb.append(String.format(" %s %s", item.isChecked() ? ":ballot_box_with_check:" : ":white_large_square:", item.getName())).append(Config.EOL);
				}
			}

			sb.append(Config.EOL);
		}
		if (sb.length() == 0) {
			sb.append("There are currently no items!");
		}
		return Config.EOL + sb.toString();
	}
}