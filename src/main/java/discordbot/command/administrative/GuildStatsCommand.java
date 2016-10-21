package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * !reboot
 * restarts the bot
 */
public class GuildStatsCommand extends AbstractCommand {
	public GuildStatsCommand(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, MessageChannel channel, User author) {
		return getTotalTable();
	}

	public String getTotalTable() {
		List<List<String>> body = new ArrayList<>();
		int totGuilds = 0, totUsers = 0, totChannels = 0, totVoice = 0, totActiveVoice = 0, totRequests = 0;
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
			totRequests += requests;
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
		return Misc.makeAsciiTable(Arrays.asList("#", "Guilds", "Users", "T-Chan", "V-Chan", "Playing on", "Requests"), body, null);
	}
}