package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.handler.GuildSettings;
import discordbot.main.DiscordBot;
import discordbot.role.RoleRankings;
import sx.blah.discord.handle.obj.IGuild;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * updates the ranking of members within a guild
 */
public class UserRankingSystemService extends AbstractService {

	public UserRankingSystemService(DiscordBot b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "user_role_ranking";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(15);
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
		List<IGuild> guilds = bot.client.getGuilds();
		for (IGuild guild : guilds) {
			GuildSettings settings = GuildSettings.get(guild);
			if (settings.getOrDefault(SettingRoleTimeRanks.class).equals("true") && RoleRankings.canModifyRoles(guild, bot.client.getOurUser())) {
				handleGuild(guild);
			}
		}
	}

	private void handleGuild(IGuild guild) {
		RoleRankings.fixForServer(guild);
		guild.getUsers().stream().filter(user -> !user.isBot()).forEach(user -> RoleRankings.assignUserRole(bot, guild, user));
	}

	@Override
	public void afterRun() {
		System.gc();
	}
}