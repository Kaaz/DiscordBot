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
	private Long expiresIn, lastAction;
	private boolean active;

	public CommandReactionListener(String userId, DataType data) {
		this.data = data;
		this.userId = userId;
		reactions = new LinkedHashMap<>();
		active = true;
		lastAction = System.currentTimeMillis();
		expiresIn = TimeUnit.MINUTES.toMillis(5);
	}

	public boolean isActive() {
		return active;
	}

	public void disable() {
		this.active = false;
	}

	/**
	 * The time after which this listener expires which is now + specified time
	 * Defaults to now+5min
	 *
	 * @param timeUnit time units
	 * @param time     amount of time units
	 */
	public void setExpiresIn(TimeUnit timeUnit, long time) {
		expiresIn = timeUnit.toMillis(time);
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
	 * updates the timestamp when the reaction was last accessed
	 */
	public void updateLastAction() {
		lastAction = System.currentTimeMillis();
	}

	/**
	 * When does this reaction listener expire?
	 *
	 * @return timestamp in millis
	 */
	public Long getExpiresInTimestamp() {
		return lastAction + expiresIn;
	}

	public String getUserId() {
		return userId;
	}
}
