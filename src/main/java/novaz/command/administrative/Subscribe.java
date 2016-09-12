package novaz.command.administrative;

import novaz.core.AbstractCommand;
import novaz.db.model.OService;
import novaz.db.model.OSubscription;
import novaz.db.model.QActiveSubscriptions;
import novaz.db.table.TChannels;
import novaz.db.table.TServers;
import novaz.db.table.TServices;
import novaz.db.table.TSubscriptions;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * !subscribe
 * subscripe to certain events
 */
public class Subscribe extends AbstractCommand {
	public Subscribe(NovaBot b) {
		super(b);
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
		return new String[]{};
	}

	@Override
	public boolean isAllowedInPrivateChannel() {
		return false;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		List<String> headers = new ArrayList<>();
		List<List<String>> tbl = new ArrayList<>();
		if (args.length == 0) {
			Collections.addAll(headers, "code", "name");
			List<QActiveSubscriptions> subscriptionsForChannel = TSubscriptions.getSubscriptionsForChannel(TChannels.getCachedId(channel.getID(), channel.getGuild().getID()));
			for (QActiveSubscriptions subscriptions : subscriptionsForChannel) {
				ArrayList<String> row = new ArrayList<>();
				row.add(subscriptions.code);
				row.add(subscriptions.displayName);
				tbl.add(row);
			}
			if (tbl.size() > 0) {
				return "Active Subscriptions" + Config.EOL +
						"This channel is currenty subscribed for: " +
						Misc.makeAsciiTable(headers, tbl);
			}
			return TextHandler.get("command_subscribe_channel_has_no_subscriptions");
		}
		if (args[0].equalsIgnoreCase("stop")) {
			if (args.length > 1) {
				OService service = TServices.findBy(args[1].trim());
				if (service.id == 0) {
					return TextHandler.get("command_subscribe_invalid_service");
				}
				OSubscription subscription = TSubscriptions.findBy(TServers.getCachedId(channel.getGuild().getID()), TChannels.getCachedId(channel.getID(), channel.getGuild().getID()), service.id);
				if (subscription.subscribed == 1) {
					subscription.subscribed = 0;
					TSubscriptions.insertOrUpdate(subscription);
					return String.format(TextHandler.get("command_subscribe_unsubscribed_success"), service.displayName);
				}
				return TextHandler.get("command_subscribe_not_subscribed");
			}
			return TextHandler.get("command_subscribe_invalid_use");
		} else if (args[0].equalsIgnoreCase("info")) {
			if (args.length > 1) {
				return "todo";
			}
			return TextHandler.get("command_subscribe_invalid_use");
		} else if (args[0].equalsIgnoreCase("list")) {
			Collections.addAll(headers, "code", "name");
			List<OService> allActive = TServices.getAllActive();
			for (OService service : allActive) {
				ArrayList<String> row = new ArrayList<>();
				row.add(service.name);
				row.add(service.displayName);
				tbl.add(row);
			}
			return "Subscriptions" + Config.EOL +
					"Possible options to subscribe to: " +
					Misc.makeAsciiTable(headers, tbl);
		}
		OService service = TServices.findBy(args[0].trim());
		if (service.id == 0) {
			return TextHandler.get("command_subscribe_invalid_service");
		}
		OSubscription subscription = TSubscriptions.findBy(TServers.getCachedId(channel.getGuild().getID()), TChannels.getCachedId(channel.getID(), channel.getGuild().getID()), service.id);
		if (subscription.subscribed == 0) {
			subscription.subscribed = 1;
			subscription.channelId = TChannels.getCachedId(channel.getID(), channel.getGuild().getID());
			subscription.serverId = TServers.getCachedId(channel.getGuild().getID());
			subscription.serviceId = service.id;
			TSubscriptions.insertOrUpdate(subscription);
			return TextHandler.get("command_subscribe_success");
		}
		return TextHandler.get("command_subscribe_already_subscribed");
	}
}