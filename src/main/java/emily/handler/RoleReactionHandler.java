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

import emily.db.controllers.CReactionRole;
import emily.db.model.OReactionRoleKey;
import emily.db.model.OReactionRoleMessage;
import emily.guildsettings.GSetting;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoleReactionHandler {
    //{guild-id, {message-id, {emoji, role-id}}
    private final Map<Long, Map<Long, Map<String, Long>>> listeners;
    private final DiscordBot discordBot;

    public RoleReactionHandler(DiscordBot discordBot) {
        this.discordBot = discordBot;
        listeners = new ConcurrentHashMap<>();
    }

    public synchronized void addMessage(long guildId, long messageId) {
        if (!listeners.containsKey(guildId)) {
            listeners.put(guildId, new ConcurrentHashMap<>());
        }
        if (!listeners.get(guildId).containsKey(messageId)) {
            listeners.get(guildId).put(messageId, new ConcurrentHashMap<>());
        }
    }

    private synchronized boolean isListening(long guildId, long messageId) {
        return listeners.containsKey(guildId) && listeners.get(guildId).containsKey(messageId);
    }

    public synchronized void removeMessage(long guildId, long id) {
        if (listeners.containsKey(guildId))
            listeners.get(guildId).remove(id);
    }

    public synchronized boolean initGuild(long guildId, boolean forceReload) {
        if (!forceReload && listeners.containsKey(guildId)) {
            return true;
        }
        if (forceReload) {
            removeGuild(guildId);
        }
        List<OReactionRoleKey> keys = CReactionRole.getKeysForGuild(guildId);
        for (OReactionRoleKey key : keys) {
            if (key.messageId <= 0) {
                continue;
            }
            addMessage(guildId, key.messageId);
            List<OReactionRoleMessage> reactions = CReactionRole.getReactionsForKey(key.id);
            for (OReactionRoleMessage r : reactions) {
                addMessageReaction(guildId, key.messageId, r.emoji, r.roleId);
            }
        }

        return false;
    }

    private void addMessageReaction(long guildId, long messageId, String emoji, long roleId) {
        listeners.get(guildId).get(messageId).put(emoji, roleId);
    }

    public synchronized void removeGuild(long guildId) {
        if (listeners.containsKey(guildId)) {
            listeners.remove(guildId);
        }
    }

    private boolean isListeningToReaction(long guildId, long msgId, String emoji) {
        return listeners.get(guildId).get(msgId).containsKey(emoji);
    }


    public synchronized boolean handle(String messageId, TextChannel channel, User invoker, MessageReaction.ReactionEmote emote, boolean isAdding) {
        long guildId = channel.getGuild().getIdLong();
        long msgId = Long.valueOf(messageId);
        initGuild(guildId, false);
        String theEmote;
        if (emote.getId() == null) {
            theEmote = emote.getName();
        } else {
            theEmote = emote.getId();
        }

        if (!isListening(guildId, msgId)) {
            return false;
        }
        if (isListeningToReaction(guildId, msgId, theEmote)) {
            Long roleId = listeners.get(guildId).get(msgId).get(theEmote);
            Role role = channel.getGuild().getRoleById(roleId);
            if (isAdding) {
                channel.getGuild().getController().addRolesToMember(channel.getGuild().getMember(invoker), role).queue();
                if (GuildSettings.getBoolFor(channel, GSetting.DEBUG)) {
                    channel.sendMessage(String.format("[DEBUG] Giving the role '%s' to %s", role.getName(), invoker.getName())).queue();
                }
            } else {
                channel.getGuild().getController().removeRolesFromMember(channel.getGuild().getMember(invoker), role).queue();
                if (GuildSettings.getBoolFor(channel, GSetting.DEBUG)) {
                    channel.sendMessage(String.format("[DEBUG] Removing the role '%s' to %s", role.getName(), invoker.getName())).queue();
                }
            }
            return true;
        }
        return false;
    }
}
