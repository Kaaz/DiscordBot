package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

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
		int channels = 0, voice = 0, users = 0, activeVoice = 0;//bot.client.getVoiceChannels().size();
		String totals = "";
		List<Guild> guilds = bot.client.getGuilds();
		List<String> header = Arrays.asList("discord-id", "name");
		List<List<String>> table = new ArrayList<>();
		for (Guild guild : guilds) {
			List<String> row = new ArrayList<>();
			row.add(guild.getId());
			row.add(guild.getName());
			table.add(row);
			channels += guild.getTextChannels().size();
			voice += guild.getVoiceChannels().size();
			users += guild.getUsers().size();
			if (bot.client.getAudioManager(guild).isConnected()) {
				activeVoice++;
			}
		}
		totals += String.format("Connected to %s guilds" + Config.EOL, guilds.size());
		totals += String.format("%s voice channels" + Config.EOL, voice);
		totals += String.format("%s text channels" + Config.EOL, channels);
		totals += String.format("%s users" + Config.EOL, users);
		totals += String.format("%s shards" + Config.EOL, bot.getContainer().getShards().length);
		if (activeVoice > 0) {
			totals += String.format("And I'm playing music on %s guilds" + Config.EOL, activeVoice);
		}
		return "Statistics! " + (bot.isCreator(author) && args.length == 1 ? Misc.makeAsciiTable(header, table) : "") + Config.EOL +
				totals;
	}
}