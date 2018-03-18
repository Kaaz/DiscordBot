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

package emily.handler;

import emily.command.meta.CommandReactionListener;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandReactionHandler {
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, CommandReactionListener<?>>> reactions;

    public CommandReactionHandler() {
        reactions = new ConcurrentHashMap<>();
    }

    public void addReactionListener(long guildId, Message message, CommandReactionListener<?> handler) {
        if (handler == null) {
            return;
        }
        if (message.getChannelType().equals(ChannelType.TEXT)) {
            if (!PermissionUtil.checkPermission(message.getTextChannel(), message.getGuild().getSelfMember(), Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }
        if (!reactions.containsKey(guildId)) {
            reactions.put(guildId, new ConcurrentHashMap<>());
        }
        if (!reactions.get(guildId).containsKey(message.getIdLong())) {
            for (String emote : handler.getEmotes()) {
                message.addReaction(emote).complete();
            }
            reactions.get(guildId).put(message.getIdLong(), handler);
        }
    }

    /**
     * Handles the reaction
     *
     * @param channel   TextChannel of the message
     * @param messageId id of the message
     * @param userId    id of the user reacting
     * @param reaction  the reaction
     */
    public void handle(TextChannel channel, long messageId, long userId, MessageReaction reaction) {
        CommandReactionListener<?> listener = reactions.get(channel.getGuild().getIdLong()).get(messageId);
        if (!listener.isActive() || listener.getExpiresInTimestamp() < System.currentTimeMillis()) {
            reactions.get(channel.getGuild().getIdLong()).remove(messageId);
        } else if (listener.hasReaction(reaction.getReactionEmote().getName()) && listener.getUserId() == userId) {
            reactions.get(channel.getGuild().getIdLong()).get(messageId).updateLastAction();
            Message message = channel.getMessageById(messageId).complete();
            listener.react(reaction.getReactionEmote().getName(), message);
        }

    }

    /**
     * Do we have an event for a message?
     *
     * @param guildId   discord guild-id of the message
     * @param messageId id of the message
     * @return do we have an handler?
     */
    public boolean canHandle(long guildId, long messageId) {
        return reactions.containsKey(guildId) && reactions.get(guildId).containsKey(messageId);
    }

    public synchronized void removeGuild(long guildId) {
        reactions.remove(guildId);
    }

    /**
     * Delete expired handlers
     */
    public synchronized void cleanCache() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<Long, ConcurrentHashMap<Long, CommandReactionListener<?>>>> iterator = reactions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Long, ConcurrentHashMap<Long, CommandReactionListener<?>>> mapEntry = iterator.next();
            mapEntry.getValue().values().removeIf(listener -> !listener.isActive() || listener.getExpiresInTimestamp() < now);
            if (mapEntry.getValue().values().isEmpty()) {
                reactions.remove(mapEntry.getKey());
            }
        }
    }
}
