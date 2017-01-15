package discordbot.command.economy;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBet;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CUser;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class BetCommand extends AbstractCommand {
	@Override
	public String getDescription() {
		return "betting";
	}

	@Override
	public String getCommand() {
		return "bet";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"bet             //check out what bets there are and where you participate in",
				"",
				"bet create <betamount> <title>      //create a bet",
				"bet option add <description>        //add an option to the bet",
				"bet option remove <key>             //remove an option",
				"bet option edit <key> <description> //edits an option",
				"bet open <1-10mhd>                   //open the bet for a limited time",
				"bet refund <user>                   //refunds the user for the bet",
				"bet cancel yesimsure                //cancel the bet & refund everyone",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		TextChannel tc = (TextChannel) channel;
		Guild guild = tc.getGuild();
		if (args.length == 0) {
			//overview here
		}
		switch (args[0].toLowerCase()) {
			case "create":
				if (args.length < 3) {
					return Template.get("command_invalid_use");
				}
				int amount = Misc.parseInt(args[1], 0);
				if (amount <= 0 || amount >= CBet.MAX_BET_AMOUNT) {
					return "plz between 1 and <" + CBet.MAX_BET_AMOUNT;
				}
				String title = Misc.joinStrings(args, 2);
				if (title.length() > 128) {
					title = title.substring(0, 127);
				}
				CBet.createBet(title, amount, CGuild.getCachedId(guild.getId()), CUser.getCachedId(author.getId()));
				return "gg wp, new bet created";
			case "option":
			case "open":
			case "refund":
			case "cancel":

		}
		/**
		 * table bets
		 * id, title, creator?, timestamp, status, price
		 */
		/**
		 * table bet_options
		 * id, bet_id, description
		 */
		/**
		 * table bet_users
		 * id, bet_option_id,
		 */


		return Template.get("command_invalid_use");
	}
}
