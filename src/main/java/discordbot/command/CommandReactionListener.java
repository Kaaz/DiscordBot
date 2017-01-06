package discordbot.command;

import net.dv8tion.jda.core.entities.Message;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CommandReactionListener<DataType> {

	private final LinkedHashMap<String, Consumer<Message>> reactions;
	private volatile DataType data;
	private final String userId;
	private Long expireTimestamp;

	public CommandReactionListener(String userId, DataType data) {
		this.data = data;
		this.userId = userId;
		reactions = new LinkedHashMap<>();
		expireTimestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
	}

	/**
	 * The time after which this listener expires which is now + specified time
	 * Defaults to now+5min
	 *
	 * @param timeUnit time units
	 * @param time     amount of time units
	 */
	public void setExpiresIn(TimeUnit timeUnit, long time) {
		expireTimestamp = System.currentTimeMillis() + timeUnit.toMillis(time);
	}

	/**
	 * Check if this listener has specified emote
	 *
	 * @param emote the emote to check for
	 * @return does this listener do anything with this emote?
	 */
	public boolean hasReaction(String emote) {
		return reactions.containsKey(emote);
	}

	/**
	 * React to the reaction :')
	 *
	 * @param emote   the emote used
	 * @param message the message bound to the reaction
	 */
	public void react(String emote, Message message) {
		reactions.get(emote).accept(message);
	}

	public DataType getData() {
		return data;
	}

	public void setData(DataType data) {
		this.data = data;
	}

	/**
	 * Register a consumer for a specified emote
	 * Multiple emote's will result in overriding the old one
	 *
	 * @param emote    the emote to respond to
	 * @param consumer the behaviour when emote is used
	 */
	public void registerReaction(String emote, Consumer<Message> consumer) {
		reactions.put(emote, consumer);
	}

	/**
	 * @return list of all emotes used in this reaction listener
	 */
	public Set<String> getEmotes() {
		return reactions.keySet();
	}

	/**
	 * When does this reaction listener expire?
	 *
	 * @return timestamp in millis
	 */
	public Long getExpireTimestamp() {
		return expireTimestamp;
	}

	public String getUserId() {
		return userId;
	}
}
