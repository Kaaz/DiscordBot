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

package emily.service;

import emily.core.AbstractService;
import emily.db.controllers.CBotEvent;
import emily.db.controllers.CBotStat;
import emily.db.model.OBotEvent;
import emily.main.BotContainer;
import emily.main.DiscordBot;
import emily.main.Launcher;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Bot meta events
 */
public class BotMetaEventsService extends AbstractService {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public BotMetaEventsService(BotContainer b) {
        super(b);
    }

    @Override
    public String getIdentifier() {
        return "bot_meta_events";
    }

    @Override
    public long getDelayBetweenRuns() {
        return TimeUnit.MINUTES.toMillis(5);
    }

    @Override
    public boolean shouldIRun() {
        return true;
    }

    @Override
    public void beforeRun() {
    }

    @Override
    public void run() {
        int lastId = Integer.parseInt("0" + getData("last_broadcast_id"));
        List<OBotEvent> events = CBotEvent.getEventsAfter(lastId);
        List<TextChannel> subscribedChannels = getSubscribedChannels();
        int totGuilds = 0, totUsers = 0, totChannels = 0, totVoice = 0, totActiveVoice = 0;
        for (DiscordBot shard : bot.getShards()) {
            List<Guild> guilds = shard.getJda().getGuilds();
            int numGuilds = guilds.size();
            int users = shard.getJda().getUsers().size();
            int channels = shard.getJda().getTextChannels().size();
            int voiceChannels = shard.getJda().getVoiceChannels().size();
            int activeVoice = 0;
            for (Guild guild : shard.getJda().getGuilds()) {
                if (guild.getAudioManager().isConnected()) {
                    activeVoice++;
                }
            }
            totGuilds += numGuilds;
            totUsers += users;
            totChannels += channels;
            totVoice += voiceChannels;
            totActiveVoice += activeVoice;
        }
        CBotStat.insert(totGuilds, totUsers, totActiveVoice);
        Launcher.log("Statistics", "bot", "meta-stats",
                "guilds", totGuilds,
                "users", totUsers,
                "channels", totChannels,
                "voice-channels", totVoice,
                "radio-channels", totActiveVoice
        );

        if (events.isEmpty()) {
            return;
        }
        for (OBotEvent event : events) {
            String output = String.format(":watch: `%s` %s %s %s", dateFormat.format(event.createdOn), event.group, event.subGroup, event.data);
            for (TextChannel channel : subscribedChannels) {
                sendTo(channel, output);
            }
            lastId = event.id;
        }
        saveData("last_broadcast_id", lastId);

    }

    @Override
    public void afterRun() {
    }
}