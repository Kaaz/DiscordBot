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

package emily.command.administrative;

import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.BotContainer;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.util.DebugUtil;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.time.DateUtils;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.GGPlot2Theme;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * !reboot
 * restarts the bot
 */
public class GuildStatsCommand extends AbstractCommand {
    public GuildStatsCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "shows some statistics";
    }

    @Override
    public String getCommand() {
        return "guildstats";
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "stats         //stats!",
                "stats mini    //minified!",
                "stats users   //graph of when users joined!",
                "stats activity//last activity per shard"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "stats"
        };
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        int tracksProcessing = bot.getContainer().downloadsProcessing();
        if (!bot.getContainer().allShardsReady()) {
            return "Not fully loaded yet!";
        }
        if (args.length == 0) {
            return "Statistics! " + (tracksProcessing > 0 ? "There are **" + tracksProcessing + "** tracks waiting to be processed" : "") + BotConfig.EOL +
                    getTotalTable(bot, false) + BotConfig.EOL + "You are on shard # " + bot.getShardId();
        }
        SimpleRank userrank = bot.security.getSimpleRank(author, channel);
        switch (args[0].toLowerCase()) {
            case "mini":
                return "Statistics! " + (tracksProcessing > 0 ? "There are **" + tracksProcessing + "** tracks waiting to be processed" : "") + BotConfig.EOL +
                        getTotalTable(bot, true);
            case "music":
                return DebugUtil.sendToHastebin(getPlayingOn(bot.getContainer(), userrank.isAtLeast(SimpleRank.BOT_ADMIN) || (args.length >= 2 && args[1].equalsIgnoreCase("guilds"))));
            case "activity":
                return lastShardActivity(bot.getContainer());
            case "users":
                if (!(channel instanceof TextChannel)) {
                    return Template.get("command_invalid_use");
                }
                TreeMap<Date, Integer> map = new TreeMap<>();
                Guild guild = ((TextChannel) channel).getGuild();
                List<Member> joins = new ArrayList<>(guild.getMembers());
                for (Member join : joins) {
                    Date time = DateUtils.round(new Date(join.getJoinDate().toInstant().toEpochMilli()), Calendar.DAY_OF_MONTH);
                    if (!map.containsKey(time)) {
                        map.put(time, 0);
                    }
                    map.put(time, map.get(time) + 1);
                }
                List<Date> xData = new ArrayList<>();
                List<Integer> yData = new ArrayList<>();
                int total = 0;
                for (Map.Entry<Date, Integer> entry : map.entrySet()) {
                    total += entry.getValue();
                    xData.add(entry.getKey());
                    yData.add(total);
                }
                XYChart chart = new XYChart(1024, 600);
                chart.setTitle("Users over time for " + guild.getName());
                chart.setXAxisTitle("Date");
                chart.setYAxisTitle("Users");
                chart.getStyler().setTheme(new GGPlot2Theme());
                XYSeries series = chart.addSeries("Users", xData, yData);
                series.setMarker(SeriesMarkers.CIRCLE);
                try {
                    File f = new File("./Sample_Chart.png");
                    BitmapEncoder.saveBitmap(chart, f.getAbsolutePath(), BitmapEncoder.BitmapFormat.PNG);
                    bot.queue.add(channel.sendFile(f), message -> f.delete());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
        }
        return "Statistics! " + (tracksProcessing > 0 ? "There are **" + tracksProcessing + "** tracks waiting to be processed" : "") + BotConfig.EOL +
                getTotalTable(bot, false);
    }

    private String getPlayingOn(BotContainer container, boolean showGuildnames) {
        int activeVoice = 0;
        int totUsersInVoice = 0, totUsersInGuilds = 0;
        List<List<String>> body = new ArrayList<>();
        for (DiscordBot discordBot : container.getShards()) {
            for (Guild guild : discordBot.getJda().getGuilds()) {
                if (guild.getAudioManager().isConnected()) {
                    activeVoice++;
                    int guildUsersInVoice = 0;
                    for (Member user : guild.getAudioManager().getConnectedChannel().getMembers()) {
                        if (user != null && !user.getUser().isBot()) {
                            guildUsersInVoice++;
                        }
                    }
                    int guildUsers = guild.getMembers().size();
                    body.add(Arrays.asList(guild.getId(), guild.getName(), "" + guildUsers, "" + guildUsersInVoice));
                    totUsersInVoice += guildUsersInVoice;
                    totUsersInGuilds += guildUsers;
                }
            }
        }
        if (activeVoice == 0) {
            return Template.get("command_stats_not_playing_music");
        }
        if (!showGuildnames) {
            return Template.get("command_stats_playing_music_on", activeVoice);
        }
        return Template.get("command_stats_playing_music_on", activeVoice) + BotConfig.EOL +
                Misc.makeAsciiTable(Arrays.asList("Discord Id", "Name", "users", "in voice"),
                        body,
                        activeVoice > 1 ? Arrays.asList("TOTAL", "" + activeVoice, "" + totUsersInGuilds, "" + totUsersInVoice) : null);
    }

    private String getTotalTable(DiscordBot bot, boolean minified) {
        List<List<String>> body = new ArrayList<>();
        int totGuilds = 0, totUsers = 0, totChannels = 0, totVoice = 0, totActiveVoice = 0;
        double totRequestPerSec = 0D;
        for (DiscordBot shard : bot.getContainer().getShards()) {
            List<Guild> guilds = shard.getJda().getGuilds();
            int numGuilds = guilds.size();
            int users = shard.getJda().getUsers().size();
            int channels = shard.getJda().getTextChannels().size();
            int voiceChannels = shard.getJda().getVoiceChannels().size();
            int activeVoice = 0;
            long requests = shard.getJda().getResponseTotal();
            for (Guild guild : shard.getJda().getGuilds()) {
                if (guild.getAudioManager().isConnected()) {
                    activeVoice++;
                }
            }
            double requestPerSec = ((double) requests) / (System.currentTimeMillis() / 1000D - bot.startupTimeStamp);
            totRequestPerSec += requestPerSec;
            totGuilds += numGuilds;
            totUsers += users;
            totChannels += channels;
            totVoice += voiceChannels;
            totActiveVoice += activeVoice;
            if (!minified) {
                body.add(Arrays.asList("" + shard.getShardId(), "" + numGuilds, "" + users, "" + channels, "" + voiceChannels, activeVoice == 0 ? "-" : "" + activeVoice, String.format("%.2f/s", requestPerSec)));
            } else {
                body.add(Arrays.asList("" + numGuilds, "" + users, "" + channels, "" + voiceChannels, activeVoice == 0 ? "-" : "" + activeVoice));
            }
        }
        List<String> header = Arrays.asList("Shard", "Guilds", "Users", "Text", "Voice", "DJ", "Requests");
        if (minified) {
            header = Arrays.asList("G", "U", "T", "V", "DJ");
        }
        if (bot.getContainer().getShards().length > 1) {
            if (minified) {
                return Misc.makeAsciiTable(header, body, Arrays.asList("" + totGuilds, "" + totUsers, "" + totChannels, "" + totVoice, "" + totActiveVoice));
            }
            return Misc.makeAsciiTable(header, body, Arrays.asList("TOTAL", "" + totGuilds, "" + totUsers, "" + totChannels, "" + totVoice, "" + totActiveVoice, String.format("%.2f/s", totRequestPerSec)));
        }
        return Misc.makeAsciiTable(header, body, null);
    }

    private String lastShardActivity(BotContainer container) {
        long now = System.currentTimeMillis();
        String msg = "Last event per shard: " + new Date(now).toString() + "\n\n";
        String comment = "";
        for (DiscordBot shard : container.getShards()) {
            if (shard == null || !shard.isReady()) {
                msg += "#shard is being reset and is reloading\n";
                continue;
            }
            long lastEventReceived = now - container.getLastAction(shard.getShardId());
            msg += String.format("#%02d: %s sec ago\n", shard.getShardId(), lastEventReceived / 1000L);
        }
        return msg + comment;
    }
}