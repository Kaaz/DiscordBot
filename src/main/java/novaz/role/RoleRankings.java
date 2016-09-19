package novaz.role;

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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 19-9-2016
 */
public class RoleRankings {

	private static final ArrayList<MemberShipRole> roles = new ArrayList<>();

	public static void init() {
		roles.add(new MemberShipRole("Spectator", new Color(0xFF6DE1), 0));
		roles.add(new MemberShipRole("Outsider", new Color(0xB7FCFF), TimeUnit.DAYS.toMillis(1L)));
		roles.add(new MemberShipRole("Lurker", new Color(0x6DDAFF), TimeUnit.DAYS.toMillis(2L)));
		roles.add(new MemberShipRole("Prospect", new Color(0x80FFBA), TimeUnit.DAYS.toMillis(4L)));
		roles.add(new MemberShipRole("Friendly", new Color(0x4AFF51), TimeUnit.DAYS.toMillis(7L)));
		roles.add(new MemberShipRole("Regular", new Color(0x3CFF39), TimeUnit.DAYS.toMillis(14L)));
		roles.add(new MemberShipRole("Honored", new Color(0xA5FF48), TimeUnit.DAYS.toMillis(21L)));
		roles.add(new MemberShipRole("Veteran", new Color(0xB5FF22), TimeUnit.DAYS.toMillis(30L)));
		roles.add(new MemberShipRole("Revered", new Color(0xDCFF2C), TimeUnit.DAYS.toMillis(60L)));
		roles.add(new MemberShipRole("Herald", new Color(0xFFD000), TimeUnit.DAYS.toMillis(90L)));
		roles.add(new MemberShipRole("Exalted", new Color(0xFF9A00), TimeUnit.DAYS.toMillis(180L)));
	}

	private static MemberShipRole getHighestRole(Long memberLengthInMilis) {
		for (int i = roles.size() - 1; i >= 0; i--) {
			if (roles.get(i).getMembershipTime() <= memberLengthInMilis) {
				return roles.get(i);
			}
		}
		return roles.get(0);
	}

	public static void fixMember(IGuild guild, IUser member) {
		List<IRole> roles = guild.getRolesForUser(member);

	}

	private static void fixForServer(IGuild guild) {
		for (int i = roles.size() - 1; i >= 0; i--) {
			try {
				fixRole(guild, roles.get(i));
			} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private static void fixRole(IGuild guild, MemberShipRole rank) throws RateLimitException, DiscordException, MissingPermissionsException {
		List<IRole> rolesByName = guild.getRolesByName(rank.getName());
		IRole role;
		if (rolesByName.size() > 0) {
			role = rolesByName.get(0);
		} else {
			role = guild.createRole();
		}
		if (!role.getName().equals(rank.getName())) {
			role.changeName(rank.getName());
		}
		if (!role.getColor().equals(rank.getColor())) {
			role.changeColor(rank.getColor());
		}
		if (!role.isHoisted()) {
			role.changeHoist(true);
		}
	}

	public static void fixRoles(List<IGuild> guilds, IDiscordClient instance) {
		for (IGuild guild : guilds) {
			boolean canModifyRoles = false;
			for (IRole ourRoles : guild.getRolesForUser(instance.getOurUser())) {
				if (ourRoles.getPermissions().contains(Permissions.MANAGE_ROLES)) {
					canModifyRoles = true;
					break;
				}
			}
			if (canModifyRoles) {
				fixForServer(guild);
			}
		}
	}
}