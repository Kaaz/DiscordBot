package discordbot.command.economy;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CBanks;
import discordbot.db.controllers.CBet;
import discordbot.db.controllers.CBetOption;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OBank;
import discordbot.db.model.OBet;
import discordbot.db.model.OBetOption;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class BetCommand extends AbstractCommand {
	@Override
	public String getDescription() {
		return "allows you to create and participate in bets";
	}

	@Override
	public String getCommand() {
		return "bet";
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"bet                    //check out what bets there are and where you participate in",
				"bet join               //info for the bet you're about to join",
				"bet join <youroption>  //join the bet with your selected option",
				"",
				"bet create <betamount> <title>      //create a bet OR edit the pending one",
				"bet option add <description>        //add an option to the bet",
				"bet option remove <key>             //remove an option",
				"bet option edit <key> <description> //edits an option",
				"bet refund <user>                   //refunds the user for the bet",
				"bet cancel yesimsure                //cancel the bet & refund everyone",
				"bet open <[1-9][mhd]>               //opens the bet for a limited time",
				"bet open <[1-9][mhd]> <[1-9][mhd]>  //opens the bet with a delay, and leave it open for x time",
				"",
				"Example: bet open 10m    //open the bet now, lasts for 10 minutes",
				"Example: bet start 2h 1d //opens the bet in 2 hours, and keeps it open for 2 days",
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
		OBank bank = CBanks.findBy(author.getId());
		int guildId = CGuild.getCachedId(guild.getId());
		if (args.length == 0) {
			String ret = "Bet overview \n\n";
			List<OBet> activeBets = CBet.getActiveBetsForGuild(guildId);
			if (activeBets.isEmpty()) {
				ret = Template.get("command_bet_no_bets");
			}
			for (OBet bet : activeBets) {
				ret += String.format("\\#%d - %s\n", bet.id, bet.title);
			}
			OBet record = CBet.getActiveBet(guildId, CUser.getCachedId(author.getId()));
			if (record.status.equals(OBet.Status.PREPARING)) {
				ret += printWipBet(record);
			}
			return ret;
		}
		switch (args[0].toLowerCase()) {
			case "create":
				if (args.length < 3) {
					return Template.get("command_invalid_use");
				}
				int amount = Misc.parseInt(args[1], 0);
				long maxBetAmount = Math.min(CBet.MAX_BET_AMOUNT, bank.currentBalance);
				if (amount <= 0 || amount >= maxBetAmount) {
					return Template.get("command_bet_amount_between", 1, maxBetAmount);
				}
				String title = Misc.joinStrings(args, 2);
				if (title.length() > 128) {
					title = title.substring(0, 127);
				}
				OBet record = CBet.getActiveBet(guildId, CUser.getCachedId(author.getId()));
				if (!record.status.equals(OBet.Status.PREPARING)) {
					return Template.get("command_bet_already_preparing");
				}
				record.title = title;
				record.price = amount;
				record.guildId = guildId;
				record.ownerId = CUser.getCachedId(author.getId());
				CBet.insert(record);
				return Template.get("command_bet_create_success");
			case "option":
			case "options":
				OBet myBet = CBet.getActiveBet(guildId, CUser.getCachedId(author.getId()));
				if (!myBet.status.equals(OBet.Status.PREPARING)) {
					return Template.get("command_bet_edit_prepare_only");
				}
				if (args.length == 1) {
					return printWipBet(myBet);
				}
				if (args.length < 3) {
					return Template.get("command_invalid_use");
				}
				switch (args[1].toLowerCase()) {
					case "edit":
						OBetOption option = CBetOption.findById(myBet.id, Misc.parseInt(args[2], -1));
						if (option.id == 0) {
							return Template.get("command_bet_option_not_found");
						}
						if (args.length < 4) {
							return Template.get("command_invalid_use");
						}
						option.description = Misc.joinStrings(args, 3);
						CBetOption.update(option);
						return printWipBet(myBet);
					case "add":
						CBetOption.addOption(myBet.id, Misc.joinStrings(args, 2));
						return printWipBet(myBet);
					case "remove":
						OBetOption toRemove = CBetOption.findById(myBet.id, Misc.parseInt(args[2], -1));
						if (toRemove.id == 0) {
							return Template.get("command_bet_option_not_found");
						}
						CBetOption.delete(toRemove);
						return printWipBet(myBet);
					default:
						return Template.get("command_invalid_use");
				}
			case "open":
			case "refund":
			case "cancel":
				return Template.get("not_implemented_yet");
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

	private String printWipBet(OBet bet) {
		String ret = "\n\n **You have a bet bet in preparation**:";
		ret += "\n\n**Title**:\n" + bet.title;
		ret += "\n\n**Cost to join**:\n";
		ret += Config.ECONOMY_CURRENCY_ICON + " " + bet.price;
		ret += "\n\n**Options**: ";
		List<OBetOption> options = CBetOption.getOptionsForBet(bet.id);
		if (options.isEmpty()) {
			ret += "\nNo options added yet!";
		}
		for (OBetOption option : options) {
			ret += String.format("\n#%s - %s", option.id, option.description);
		}
		return ret;
	}
}
