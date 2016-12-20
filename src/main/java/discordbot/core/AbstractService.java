package discordbot.core;

import discordbot.db.controllers.CChannels;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CServiceVariables;
import discordbot.db.controllers.CServices;
import discordbot.db.controllers.CSubscriptions;
import discordbot.db.model.OChannel;
import discordbot.db.model.OServiceVariable;
import discordbot.db.model.OSubscription;
import discordbot.db.model.QActiveSubscriptions;
import discordbot.main.BotContainer;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractService {
	protected BotContainer bot;
	private Map<String, OServiceVariable> cache;
	private long cachedLastRun = 0L;

	public AbstractService(BotContainer b) {
		bot = b;
		cache = new HashMap<>();
	}

	/**
	 * retrieves a list of subscribed channels for service
	 *
	 * @return list of {@link TextChannel} channels
	 */

	public List<TextChannel> getSubscribedChannels() {
		List<TextChannel> channels = new ArrayList<>();
		List<QActiveSubscriptions> subscriptionsForService = CSubscriptions.getSubscriptionsForService(CServices.getCachedId(getIdentifier()));
		for (QActiveSubscriptions activeSubscriptions : subscriptionsForService) {
			OChannel databaseChannel = CChannels.findById(activeSubscriptions.channelId);
			DiscordBot botInstance = bot.getShardFor(CGuild.getCachedDiscordId(activeSubscriptions.guildId));
			TextChannel botChannel = botInstance.client.getTextChannelById(databaseChannel.discord_id);
			if (botChannel != null) {
				channels.add(botChannel);
			} else {
				OSubscription subscription = CSubscriptions.findBy(databaseChannel.server_id, databaseChannel.id, CServices.getCachedId(getIdentifier()));
				subscription.subscribed = 0;
				CSubscriptions.insertOrUpdate(subscription);
				botInstance.getContainer().reportError(new Exception("Subscription channel not found"),
						"result", "Now unsubscribed!",
						"channelID", databaseChannel.discord_id,
						"subscription", getIdentifier());
			}
		}
		return channels;
	}

	protected void sendTo(TextChannel channel, MessageEmbed message) {
		channel.sendMessage(message).queue();
	}

	protected void sendTo(TextChannel channel, String message) {
		this.bot.getShardFor(channel.getGuild().getId()).out.sendAsyncMessage(channel, message, null);
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
			cache.put(key, CServiceVariables.findBy(getIdentifier(), key));
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
		dataObject.serviceId = CServices.getCachedId(getIdentifier());
		dataObject.value = String.valueOf(value);
		CServiceVariables.insertOrUpdate(dataObject);
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
