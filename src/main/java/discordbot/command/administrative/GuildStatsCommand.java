package discordbot.command.administrative;

import com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import org.apache.commons.lang3.time.DateUtils;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.GGPlot2Theme;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"stats"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (args.length == 0) {
			return getTotalTable(bot);
		}
		SimpleRank userrank = bot.security.getSimpleRank(author, channel);
		switch (args[0].toLowerCase()) {
			case "music":
				return getPlayingOn(bot, userrank.isAtLeast(SimpleRank.BOT_ADMIN) && args.length >= 2 && args[1].equalsIgnoreCase("guilds"));
			case "users":
				if (!(channel instanceof TextChannel)) {
					return Template.get("command_invalid_use");
				}
				TreeMap<Date, Integer> map = new TreeMap<>();
				Guild guild = ((TextChannel) channel).getGuild();
				List<User> joins = new ArrayList<>(guild.getUsers());
				Collections.sort(joins, (User a, User b) -> guild.getJoinDateForUser(a).compareTo(guild.getJoinDateForUser(b)));
				for (User join : joins) {
					Date time = DateUtils.round(new Date(guild.getJoinDateForUser(join).toInstant().toEpochMilli()), Calendar.DAY_OF_MONTH);
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
					channel.sendFileAsync(f, null, message -> f.delete());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return "";
		}
		return getTotalTable(bot);
	}

	private String getPlayingOn(DiscordBot bot, boolean showGuildnames) {
		int activeVoice = 0;
		ArrayList<String> guildnames = new ArrayList<>();
		for (DiscordBot discordBot : bot.getContainer().getShards()) {
			for (Guild guild : discordBot.client.getGuilds()) {
				if (discordBot.client.getAudioManager(guild).isConnected()) {
					activeVoice++;
					guildnames.add(guild.getName() +" size["+guild.getUsers().size()+"] channel["+discordBot.client.getAudioManager(guild).getConnectedChannel().getUsers().size()+"]");
				}
			}
		}
		if (activeVoice == 0) {
			return Template.get("command_stats_not_playing_music");
		}
		if (!showGuildnames) {
			return Template.get("command_stats_playing_music_on", activeVoice);
		}
		return Template.get("command_stats_playing_music_on", activeVoice) + Config.EOL + Joiner.on(", ").join(guildnames);
	}

	private String getTotalTable(DiscordBot bot) {
		List<List<String>> body = new ArrayList<>();
		int totGuilds = 0, totUsers = 0, totChannels = 0, totVoice = 0, totActiveVoice = 0;
		double totRequestPerSec = 0D;
		for (DiscordBot shard : bot.getContainer().getShards()) {
			List<Guild> guilds = shard.client.getGuilds();
			int numGuilds = guilds.size();
			int users = shard.client.getUsers().size();
			int channels = shard.client.getTextChannels().size();
			int voiceChannels = shard.client.getVoiceChannels().size();
			int activeVoice = 0;
			int requests = shard.client.getResponseTotal();
			for (Guild guild : shard.client.getGuilds()) {
				if (bot.client.getAudioManager(guild).isConnected()) {
					activeVoice++;
				}
			}
			double requestPerSec = ((double) requests) / ((double) (System.currentTimeMillis() / 1000D - bot.startupTimeStamp));
			totRequestPerSec += requestPerSec;
			totGuilds += numGuilds;
			totUsers += users;
			totChannels += channels;
			totVoice += voiceChannels;
			totActiveVoice += activeVoice;
			body.add(Arrays.asList("" + shard.getShardId(), "" + numGuilds, "" + users, "" + channels, "" + voiceChannels, activeVoice == 0 ? "-" : "" + activeVoice, String.format("%.2f/s", requestPerSec)));
		}
		if (bot.getContainer().getShards().length > 1) {
			return Misc.makeAsciiTable(Arrays.asList("Shard", "Guilds", "Users", "T-Chan", "V-Chan", "Music", "Requests"), body, Arrays.asList("TOTAL", "" + totGuilds, "" + totUsers, "" + totChannels, "" + totVoice, "" + totActiveVoice, String.format("%.2f/s", totRequestPerSec)));
		}
		return Misc.makeAsciiTable(Arrays.asList("Shard", "Guilds", "Users", "T-Chan", "V-Chan", "Playing on", "Requests"), body, null);
	}
}