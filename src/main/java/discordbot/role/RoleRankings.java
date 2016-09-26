package discordbot.role;

import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.guildsettings.defaults.SettingRoleTimeRanksPrefix;
import discordbot.handler.GuildSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 19-9-2016
 */
public class RoleRankings {

	private static final ArrayList<MemberShipRole> roles = new ArrayList<>();
	private static final Set<String> roleNames = new HashSet<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleRankings.class);

	public static void init() {
		roles.add(new MemberShipRole("Spectator", new Color(0xFF6DE1), 0));
		roles.add(new MemberShipRole("Outsider", new Color(0xD7ACFF), TimeUnit.HOURS.toMillis(1L)));
		roles.add(new MemberShipRole("Lurker", new Color(0x61D3FF), TimeUnit.HOURS.toMillis(4L)));
		roles.add(new MemberShipRole("Prospect", new Color(0x53CAFF), TimeUnit.DAYS.toMillis(1L)));
		roles.add(new MemberShipRole("Friendly", new Color(0x4AFF51), TimeUnit.DAYS.toMillis(3L)));
		roles.add(new MemberShipRole("Regular", new Color(0x3CFF39), TimeUnit.DAYS.toMillis(7L)));
		roles.add(new MemberShipRole("Honored", new Color(0xA5FF48), TimeUnit.DAYS.toMillis(14L)));
		roles.add(new MemberShipRole("Veteran", new Color(0xB5FF22), TimeUnit.DAYS.toMillis(30L)));
		roles.add(new MemberShipRole("Revered", new Color(0xDCFF2C), TimeUnit.DAYS.toMillis(60L)));
		roles.add(new MemberShipRole("Herald", new Color(0xFFD000), TimeUnit.DAYS.toMillis(90L)));
		roles.add(new MemberShipRole("Exalted", new Color(0xFF9A00), TimeUnit.DAYS.toMillis(180L)));
		for (MemberShipRole role : roles) {
			roleNames.add(role.getName().toLowerCase());
		}
	}

	/**
	 * retrieves a list of all membership roles
	 *
	 * @return list
	 */
	public static List<MemberShipRole> getAllRoles() {
		return roles;
	}

	/**
	 * Prefixes the role name based on the guild's setting
	 *
	 * @param guild the guild
	 * @param role  the role
	 * @return full name
	 */
	public static String getFullName(IGuild guild, MemberShipRole role) {
		return getPrefix(guild) + " " + role.getName();
	}

	public static MemberShipRole getHighestRole(Long memberLengthInMilis) {
		for (int i = roles.size() - 1; i >= 0; i--) {
			if (roles.get(i).getMembershipTime() <= memberLengthInMilis) {
				return roles.get(i);
			}
		}
		return roles.get(0);
	}


	/**
	 * Attempts to fix create the membership roles for a guild
	 *
	 * @param guild the guild to create/modify the roles for
	 */
	public static void fixForServer(IGuild guild) {
		for (int i = roles.size() - 1; i >= 0; i--) {
			try {
				fixRole(guild, roles.get(i));
			} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	public static String getPrefix(IGuild guild) {
		return GuildSettings.get(guild).getOrDefault(SettingRoleTimeRanksPrefix.class);
	}

	/**
	 * Fixes adds/modifies a membership role to match the settings
	 *
	 * @param guild the guild to add/modify the role for
	 * @param rank  the role to add/modify
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	private static void fixRole(IGuild guild, MemberShipRole rank) throws RateLimitException, DiscordException, MissingPermissionsException {
		List<IRole> rolesByName = guild.getRolesByName(getFullName(guild, rank));
		IRole role;
		if (rolesByName.size() > 0) {
			role = rolesByName.get(0);
		} else {
			role = guild.createRole();
		}
		if (!role.getName().equals(getFullName(guild, rank))) {
			role.changeName(getFullName(guild, rank));
		}
		if (!role.getColor().equals(rank.getColor())) {
			role.changeColor(rank.getColor());
		}
		if (!role.isHoisted()) {
			role.changeHoist(true);
		}
	}

	/**
	 * checks if a user has the manage roles permission
	 *
	 * @param guild   the guild to check
	 * @param ourUser the user to check for
	 * @return has the manage roles premission?
	 */
	public static boolean canModifyRoles(IGuild guild, IUser ourUser) {

		for (IRole ourRoles : guild.getRolesForUser(ourUser)) {
			if (ourRoles.getPermissions().contains(Permissions.MANAGE_ROLES)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * deletes the created roles
	 *
	 * @param guild   the guild to clean up
	 * @param ourUser the bot user
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public static void cleanUpRoles(IGuild guild, IUser ourUser) throws RateLimitException, DiscordException, MissingPermissionsException {
		if (!canModifyRoles(guild, ourUser)) {
			return;
		}
		for (IRole role : guild.getRoles()) {
			if (role.getName().contains(getPrefix(guild))) {
				role.delete();
			} else if (roleNames.contains(role.getName().toLowerCase())) {
				try {
					role.delete();
				} catch (MissingPermissionsException ignored) {
					LOGGER.info("Can't delete role %s! In the guild %s", role.getName(), guild.getName());
				}
			}
		}
	}

	/**
	 * Attempts to fix create the membership roles for all guilds
	 *
	 * @param guilds   the guilds to fix the roles for
	 * @param instance the bot instance
	 */
	public static void fixRoles(List<IGuild> guilds, IDiscordClient instance) {
		for (IGuild guild : guilds) {
			if (!GuildSettings.get(guild).getOrDefault(SettingRoleTimeRanks.class).equals("true")) {
				continue;
			}
			if (canModifyRoles(guild, instance.getOurUser())) {
				fixForServer(guild);
			}
		}
	}
}