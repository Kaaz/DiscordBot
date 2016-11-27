package discordbot.command.informative;

import discordbot.command.CooldownScope;
import discordbot.command.ICommandCooldown;
import discordbot.core.AbstractCommand;
import discordbot.handler.CommandHandler;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.DisUtil;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;
import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;

import java.util.List;


/**
 * !info
 * some general information about the bot
 */
public class Info extends AbstractCommand implements ICommandCooldown {
	private Trello trello;

	public Info() {
		super();
		trello = new TrelloImpl(Config.TRELLO_API_KEY, Config.TRELLO_TOKEN);
	}

	@Override
	public boolean isBlacklistable() {
		return false;
	}

	@Override
	public long getCooldownDuration() {
		return 15L;
	}

	@Override
	public CooldownScope getScope() {
		return CooldownScope.CHANNEL;
	}

	@Override
	public String getDescription() {
		return "Shows some general information about me and my future plans.";
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
		return new String[]{
				"about"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
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
				default:
					break;
			}
		}
		String onlineFor = TimeUtil.getRelativeTime(bot.startupTimeStamp, false);
		String response = bot.chatBotHandler.chat("What are you?");
		String prefix = DisUtil.getCommandPrefix(channel);
		if (response.isEmpty()) {
			response = "I'm batman";
		}
		return "What am I? *" + response + "* " + Config.EOL +
				"Currently active on " + bot.client.getGuilds().size() + " guilds and the last time I restarted was  " + onlineFor + "." + Config.EOL +
				"Running version `" + Launcher.getVersion().toString() + "` and there are " + CommandHandler.getCommands().length + " commands I can perform type **" + prefix + "help** for a full list" + Config.EOL +
				"If I can't help you out, you can always try your luck in my __" + prefix + "discord__";
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