/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.core;

import emily.db.controllers.CChannels;
import emily.db.controllers.CGuild;
import emily.db.controllers.CServiceVariables;
import emily.db.controllers.CServices;
import emily.db.controllers.CSubscriptions;
import emily.db.model.OChannel;
import emily.db.model.OServiceVariable;
import emily.db.model.OSubscription;
import emily.db.model.QActiveSubscriptions;
import emily.main.BotContainer;
import emily.main.DiscordBot;
import emily.main.Launcher;
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
            TextChannel botChannel = botInstance.getJda().getTextChannelById(databaseChannel.discord_id);
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
        bot.getShardFor(channel.getGuild().getId()).queue.add(channel.sendMessage(message));
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
            try {
                run();
            } catch (Exception e) {
                Launcher.logToDiscord(e, "service", getIdentifier());
            }
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
    public abstract void run() throws Exception;

    /**
     * called after run(), can be used to clean up things if needed
     */
    public abstract void afterRun();
}
