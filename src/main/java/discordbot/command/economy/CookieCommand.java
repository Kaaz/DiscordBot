package discordbot.command.economy;

import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBanks;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OBank;
import discordbot.db.model.OUser;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CookieCommand extends AbstractCommand {
	public CookieCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Ask for a cookie";
	}

	@Override
	public String getCommand() {
		return "cookie";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"cookie             //gives you a cookie"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"candy",
		};
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		OUser user = CUser.findBy(author.getId());
		if (user.id == 0) {
			return Template.get("cant_find_user", author.getName());
		}
		OBank userAccount = CBanks.findBy(author.getId());
		if (userAccount.currentBalance > CBanks.CURRENCY_NO_HELP_AFTER) {
			return "not helping you anymore";
		}
		double now = (System.currentTimeMillis() / 1000D);
		double time = now - user.lastCurrencyRetrieval;
		int income = (int) Math.min(time * (CBanks.CURRENCY_PER_HOUR / 3600D), CBanks.CURRENCY_GIVEAWAY_MAX);
		int lastCurrencyRetrieval = user.lastCurrencyRetrieval;
		if (income == 0) {
			return String.format("no %s for you yet, try again in %s",
					Config.ECONOMY_CURRENCY_ICON, TimeUtil.getRelativeTime((long) (now + 1 + CBanks.SECONDS_PER_CURRENCY - (now - lastCurrencyRetrieval)), false, false))
					+ getFooter();
		}
		if (income == CBanks.CURRENCY_GIVEAWAY_MAX) {
			lastCurrencyRetrieval = (int) now;
		} else {
			lastCurrencyRetrieval += income * CBanks.SECONDS_PER_CURRENCY;
		}
		if (!CBanks.getBotAccount().transferTo(userAccount, income, "Charity")) {
			Launcher.logToDiscord(new Exception("BANK_TRANSFER"), "from", "bot", "toAccount", userAccount.id);
		}
		user.lastCurrencyRetrieval = lastCurrencyRetrieval;
		CUser.update(user);
		return String.format("you get %s cookies and your time is updated to %s (now=%s)",
				income, lastCurrencyRetrieval, (int) now) +
				getFooter();
	}

	private String getFooter() {
		return Config.EOL +
				String.format("You can retrieve a %s every %s minutes, you don't have to retrieve them directly, I'll up to %s %s for you.",
						Config.ECONOMY_CURRENCY_NAME, (int) (CBanks.SECONDS_PER_CURRENCY / 60), CBanks.CURRENCY_GIVEAWAY_MAX, Config.ECONOMY_CURRENCY_NAMES);
	}
}
