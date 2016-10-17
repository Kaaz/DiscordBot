package discordbot.core;

import discordbot.db.model.OChannel;
import discordbot.db.model.OServiceVariable;
import discordbot.db.model.OSubscription;
import discordbot.db.model.QActiveSubscriptions;
import discordbot.db.table.TChannels;
import discordbot.db.table.TServiceVariables;
import discordbot.db.table.TServices;
import discordbot.db.table.TSubscriptions;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractService {
	protected DiscordBot bot;
	private Map<String, OServiceVariable> cache;
	private long cachedLastRun = 0L;

	public AbstractService(DiscordBot b) {
		bot = b;
		cache = new HashMap<>();
	}

	/**
	 * retrieves a list of subscribed channels for service
	 *
	 * @return list of {@link TextChannel} channels
	 */
	public static List<TextChannel> getSubscribedChannels(DiscordBot bot, int serviceId) {
		List<TextChannel> channels = new ArrayList<>();
		List<QActiveSubscriptions> subscriptionsForService = TSubscriptions.getSubscriptionsForService(serviceId);
		for (QActiveSubscriptions activeSubscriptions : subscriptionsForService) {
			OChannel databaseChannel = TChannels.findById(activeSubscriptions.channelId);
			TextChannel botChannel = bot.client.getTextChannelById(databaseChannel.discord_id);
			if (botChannel != null) {
				channels.add(botChannel);
			} else {
				OSubscription subscription = TSubscriptions.findBy(databaseChannel.server_id, databaseChannel.id, serviceId);
				subscription.subscribed = 0;
				TSubscriptions.insertOrUpdate(subscription);
				bot.out.sendErrorToMe(new Exception("Subscription channel not found"),
						"result", "Now unsubscribed!",
						"channelID", databaseChannel.discord_id,
						"subscription", serviceId);
			}
		}
		return channels;
	}

	public List<TextChannel> getSubscribedChannels() {
		return getSubscribedChannels(bot, TServices.getCachedId(getIdentifier()));
	}

	/**
	 * Start the service
	 */
	public final void start() {
		if (cachedLastRun == 0L) {
			cachedLastRun = Long.parseLong("0" + getData("abs_last_service_run"));
		}
		long now = System.currentTimeMillis();
		long next = cachedLastRun + getDelayBetweenRuns();
		if (next <= now) {
			if (!shouldIRun()) {
				return;
			}
			beforeRun();
			run();
			afterRun();
			saveData("abs_last_service_run", now);
			cachedLastRun = now;
		}
	}

	/**
	 * gets data for a certain key and caches it
	 *
	 * @param key key used
	 * @return the value of the key
	 */
	protected String getData(String key) {
		return getDataObject(key).value;
	}

	/**
	 * gets data for a certain key and caches it
	 *
	 * @param key key used
	 * @return the database row object for
	 */
	private OServiceVariable getDataObject(String key) {
		if (!cache.containsKey(key)) {
			cache.put(key, TServiceVariables.findBy(getIdentifier(), key));
		}
		return cache.get(key);
	}


	/**
	 * saves service data
	 *
	 * @param key   the key
	 * @param value Any value converted to string
	 */
	protected void saveData(String key, Object value) {
		OServiceVariable dataObject = getDataObject(key);
		dataObject.variable = key;
		dataObject.serviceId = TServices.getCachedId(getIdentifier());
		dataObject.value = String.valueOf(value);
		TServiceVariables.insertOrUpdate(dataObject);
	}

	/**
	 * The identifier of the service. This is used to reference the service and the key to store data with.
	 *
	 * @return the identifier of the service
	 */
	public abstract String getIdentifier();

	/**
	 * milliseconds it should wait before attempting another run
	 *
	 * @return delay in milliseconds
	 */
	public abstract long getDelayBetweenRuns();

	/**
	 * Determines if the service should run
	 *
	 * @return should it run?
	 */
	public abstract boolean shouldIRun();

	/**
	 * called before run, so things can be prepared if needed
	 */
	public abstract void beforeRun();

	/**
	 * the actual logic of the service
	 */
	public abstract void run();

	/**
	 * called after run(), can be used to clean up things if needed
	 */
	public abstract void afterRun();
}
