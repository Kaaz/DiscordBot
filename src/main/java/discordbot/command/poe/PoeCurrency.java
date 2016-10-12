package discordbot.command.poe;

import discordbot.core.AbstractCommand;
import discordbot.db.model.OPoEToken;
import discordbot.db.table.TPoEToken;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import org.libpoe.model.StashTab;
import org.libpoe.model.item.Item;
import org.libpoe.model.property.MinMaxProperty;
import org.libpoe.model.property.Property;
import org.libpoe.net.AuthInfo;
import org.libpoe.net.DataReader;
import org.libpoe.util.League;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * !poeitem
 * Analyzes an item from path of exile
 */
public class PoeCurrency extends AbstractCommand {
	public PoeCurrency(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Returns a list of currency on your account";
	}

	@Override
	public String getCommand() {
		return "poec";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"poec                   //returns list of currency for default league",
				"poec token <token>     //sets the session token",
				"poec league <league>   //currency for league",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("token")) {
				OPoEToken token = TPoEToken.findBy(author.getID());
				token.session_id = args[1];
				TPoEToken.insertOrUpdate(token);
				return "Updated your token!";
			} else if (args[0].equalsIgnoreCase("league")) {
				return "not implemented yet sorry boys!";
			}
			return Template.get("command_invalid_usage");
		}
		OPoEToken token = TPoEToken.findBy(author.getID());
		AuthInfo account = new AuthInfo(token.session_id);
		DataReader reader = new DataReader(account);
		if (!reader.authenticate()) {
			return "Your token is not valid :(";
		}
		bot.out.sendAsyncMessage(channel, "Fetching data this might take a minute!", null);
		HashMap<String, Integer> currency = new HashMap<>();
		int max = 1;
		for (int i = 0; i < max; i++) {
			StashTab stashTab = null;
			try {
				stashTab = reader.getStashTab(League.ESSENCE.getId(), i);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (stashTab != null) {
				if (max == 1) {
					max = stashTab.getNumTabs();
				}
				System.out.println((i + 1) + " out of " + stashTab.getNumTabs());
				for (Item item : stashTab.getItems()) {
					if (item.getTypeLine().toLowerCase().contains("orb")) {
						Property property = item.getProperty("Stack Size");
						if (property instanceof MinMaxProperty) {
							MinMaxProperty p = (MinMaxProperty) property;
							if (!currency.containsKey(item.getTypeLine())) {
								currency.put(item.getTypeLine(), 0);
							}
							currency.put(item.getTypeLine(), currency.get(item.getTypeLine()) + p.getMinValue());
						}
					}
				}
			}
		}
		String text = "Checking your currency in PoE!" + Config.EOL;
		List<List<String>> tbl = new ArrayList<>();
		Misc.sortByValue(currency).forEach((k, v) -> {
			ArrayList<String> row = new ArrayList<>();
			row.add(k);
			row.add(String.valueOf(v));
			tbl.add(row);
		});
		text += Misc.makeAsciiTable(Arrays.asList("Currency", "#"), tbl);
		return text;
	}
}