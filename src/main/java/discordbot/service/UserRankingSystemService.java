package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.db.model.OGuildMember;
import discordbot.db.table.TGuildMember;
import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.handler.GuildSettings;
import discordbot.main.NovaBot;
import discordbot.role.MemberShipRole;
import discordbot.role.RoleRankings;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * updates the ranking of members within a guild
 */
public class UserRankingSystemService extends AbstractService {

	public UserRankingSystemService(NovaBot b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "user_role_ranking";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.HOURS.toMillis(1);
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
		List<IGuild> guilds = bot.instance.getGuilds();
		for (IGuild guild : guilds) {
			GuildSettings settings = GuildSettings.get(guild);
			if (settings.getOrDefault(SettingRoleTimeRanks.class).equals("true") && RoleRankings.canModifyRoles(guild, bot.instance.getOurUser())) {
				handleGuild(guild);
			}

		}
	}

	private void handleGuild(IGuild guild) {
		RoleRankings.fixForServer(guild);
		for (IUser user : guild.getUsers()) {
			if (!user.isBot()) {
				handleUser(guild, user);
			}
		}
	}

	private void handleUser(IGuild guild, IUser user) {
		List<IRole> roles = user.getRolesForGuild(guild);
		OGuildMember membership = TGuildMember.findBy(guild.getID(), user.getID());
		boolean hasTargetRole = false;
		String prefix = RoleRankings.getPrefix(guild);
		if (membership.joinDate == null) {
			membership.joinDate = new Timestamp(System.currentTimeMillis());
			TGuildMember.insertOrUpdate(membership);
		}
		MemberShipRole targetRole = RoleRankings.getHighestRole(System.currentTimeMillis() - membership.joinDate.getTime());
		for (IRole role : roles) {
			if (role.getName().startsWith(prefix)) {
				if (role.getName().equals(RoleRankings.getFullName(guild, targetRole))) {
					hasTargetRole = true;
				} else {
					try {
						user.removeRole(role);
					} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
						e.printStackTrace();
						bot.out.sendErrorToMe(e, "server", guild.getName(), "user", user.getName());
					}
				}
			}
		}
		if (!hasTargetRole) {
			List<IRole> roleList = guild.getRolesByName(RoleRankings.getFullName(guild, targetRole));
			if (roleList.size() > 0) {
				try {
					user.addRole(roleList.get(0));
				} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
					bot.out.sendErrorToMe(e, "server", guild.getName(), "user", user.getName());
				}
			} else {
				bot.out.sendErrorToMe(new Exception("Role not found"), "guild", guild.getName(), "user", user.getName());
			}
		}
	}

	@Override
	public void afterRun() {
	}
}