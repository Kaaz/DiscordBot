package discordbot.command.administrative;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OService;
import discordbot.db.model.OSubscription;
import discordbot.db.model.QActiveSubscriptions;
import discordbot.db.controllers.CChannels;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CServices;
import discordbot.db.controllers.CSubscriptions;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * !subscribe
 * subscripe to certain events
 */
public class Subscribe extends AbstractCommand {
	public Subscribe() {
		super();
	}

	@Override
	public String getDescription() {
		return "subscribe the channel to certain events";
	}

	@Override
	public String getCommand() {
		return "subscribe";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"subscribe                //check what subscriptions are active",
				"subscribe <name>         //subscribe to subject",
				"subscribe stop <name>    //stop subscription to subject",
				"subscribe info <name>    //information about subject",
				"subscribe list           //See what subscription options there are",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"sub"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		TextChannel txt = (TextChannel) channel;
		List<String> headers = new ArrayList<>();
		List<List<String>> tbl = new ArrayList<>();
		if (args.length == 0) {
			Collections.addAll(headers, "code", "name");
			List<QActiveSubscriptions> subscriptionsForChannel = CSubscriptions.getSubscriptionsForChannel(CChannels.getCachedId(txt.getId(), txt.getGuild().getId()));
			for (QActiveSubscriptions subscriptions : subscriptionsForChannel) {
				ArrayList<String> row = new ArrayList<>();
				row.add(subscriptions.code);
				row.add(subscriptions.displayName);
				tbl.add(row);
			}
			if (tbl.size() > 0) {
				return "Active Subscriptions" + Config.EOL +
						"This channel is currenty subscribed for: " +
						Misc.makeAsciiTable(headers, tbl, null);
			}
			return Template.get("command_subscribe_channel_has_no_subscriptions") + Config.EOL +
					"Possible options to subscribe to: " +
					getServicesTable();
		}
		if (args[0].equalsIgnoreCase("stop")) {
			if (args.length > 1) {
				OService service = CServices.findBy(args[1].trim());
				if (service.id == 0) {
					return Template.get("command_subscribe_invalid_service");
				}
				OSubscription subscription = CSubscriptions.findBy(CGuild.getCachedId(txt.getGuild().getId()), CChannels.getCachedId(channel.getId(), txt.getGuild().getId()), service.id);
				if (subscription.subscribed == 1) {
					subscription.subscribed = 0;
					CSubscriptions.insertOrUpdate(subscription);
					return Template.get("command_subscribe_unsubscribed_success", service.displayName);
				}
				return Template.get("command_subscribe_not_subscribed");
			}
			return Template.get("command_subscribe_invalid_use");
		} else if (args[0].equalsIgnoreCase("info")) {
			if (args.length > 1) {
				return "todo";
			}
			return Template.get("command_subscribe_invalid_use");
		} else if (args[0].equalsIgnoreCase("list")) {
			return "Subscriptions" + Config.EOL +
					"Possible options to subscribe to: " +
					getServicesTable();
		}
		OService service = CServices.findBy(args[0].trim());
		if (service.id == 0) {
			return Template.get("command_subscribe_invalid_service");
		}
		OSubscription subscription = CSubscriptions.findBy(CGuild.getCachedId(txt.getGuild().getId()), CChannels.getCachedId(channel.getId(), ((TextChannel) channel).getGuild().getId()), service.id);
		if (subscription.subscribed == 0) {
			subscription.subscribed = 1;
			subscription.channelId = CChannels.getCachedId(channel.getId(), txt.getGuild().getId());
			subscription.serverId = CGuild.getCachedId(txt.getGuild().getId());
			subscription.serviceId = service.id;
			CSubscriptions.insertOrUpdate(subscription);
			return Template.get("command_subscribe_success");
		}
		return Template.get("command_subscribe_already_subscribed");
	}

	private String getServicesTable() {
		List<List<String>> table = new ArrayList<>();
		List<OService> allActive = CServices.getAllActive();
		for (OService service : allActive) {
			ArrayList<String> row = new ArrayList<>();
			row.add(service.name);
			row.add(service.displayName);
			table.add(row);
		}
		return Misc.makeAsciiTable(Arrays.asList("code", "name"), table, null);
	}
}