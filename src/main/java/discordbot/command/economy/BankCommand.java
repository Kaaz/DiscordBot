package discordbot.command.economy;

import discordbot.core.AbstractCommand;
import discordbot.db.model.OBank;
import discordbot.db.controllers.CBanks;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

public class BankCommand extends AbstractCommand {
	public BankCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "For all your banking needs";
	}

	@Override
	public String getCommand() {
		return "jar";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"jar                       //shows current balance",
				"jar history               //shows last transactions",
				"jar donate @user <amount> //donates <amount> to @user ",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"currency",
				"money",
				"bank",
		};
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		OBank bank = CBanks.findBy(author.getId());

		return String.format("Your current balance is `%s` %s ", bank.currentBalance, Config.ECONOMY_CURRENCY_ICON);
	}
}
