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

import emily.db.controllers.CGuild;
import emily.db.controllers.CReplyPattern;
import emily.db.model.OReplyPattern;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the automatic responses to messages
 */
public class AutoReplyHandler {
    private final Map<Long, Long[]> cooldowns;
    private DiscordBot bot;
    private volatile AutoReply[] replies;

    public AutoReplyHandler(DiscordBot bot) {
        this.bot = bot;
        cooldowns = new ConcurrentHashMap<>();
        reload();
    }

    public void removeGuild(long discordGuildId) {
        if (cooldowns.containsKey(discordGuildId)) {
            cooldowns.remove(discordGuildId);
        }
    }

    public boolean autoReplied(Message message) {
        if (message.getChannel() instanceof PrivateChannel) {
            return false;
        }
        if (!(message.getChannel() instanceof TextChannel)) {
            return false;
        }
        TextChannel channel = (TextChannel) message.getChannel();
        long guildId = channel.getGuild().getIdLong();
        int internalGuildId = CGuild.getCachedId(guildId);
        Long now = System.currentTimeMillis();
        for (int index = 0; index < replies.length; index++) {
            if (replies[index].guildId == 0 || replies[index].guildId == internalGuildId) {
                Long lastUse = getCooldown(guildId, index);
                if (lastUse + replies[index].cooldown < now) {
                    Matcher matcher = replies[index].pattern.matcher(message.getContentRaw());
                    if (matcher.find()) {
                        saveCooldown(guildId, index, now);
                        bot.out.sendAsyncMessage(channel, message.getAuthor().getAsMention() + ", " + replies[index].reply, null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private long getCooldown(long guildId, int index) {
        if (!cooldowns.containsKey(guildId)) {
            cooldowns.put(guildId, new Long[replies.length]);
        }
        if (index >= cooldowns.get(guildId).length || cooldowns.get(guildId)[index] == null) {
            return 0;
        }
        return cooldowns.get(guildId)[index];
    }

    private void saveCooldown(long guildId, int index, long value) {
        if (!cooldowns.containsKey(guildId)) {
            cooldowns.put(guildId, new Long[replies.length]);
        }
        if (cooldowns.get(guildId).length != replies.length) {
            cooldowns.put(guildId, Arrays.copyOf(cooldowns.get(guildId), replies.length));
        }
        cooldowns.get(guildId)[index] = value;
    }

    public void reload() {
        List<OReplyPattern> all = CReplyPattern.getAll();
        List<AutoReply> list = new ArrayList<>();
        int index = 0;
        for (OReplyPattern reply : all) {
            AutoReply ar = new AutoReply();
            if (reply.pattern == null || reply.pattern.length() < 5) {
                continue;
            }
            ar.pattern = Pattern.compile(reply.pattern, Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
            ar.tag = reply.tag;
            ar.cooldown = reply.cooldown;
            ar.reply = reply.reply;
            ar.guildId = reply.guildId;
            list.add(ar);
        }
        replies = list.toArray(new AutoReply[list.size()]);
    }

    private class AutoReply {

        public Pattern pattern;
        public String tag;
        public long cooldown;
        public String reply;
        public int guildId;
    }
}
