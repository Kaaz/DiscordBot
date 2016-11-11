package discordbot.handler;

import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CRank;
import discordbot.db.controllers.CUser;
import discordbot.db.controllers.CUserRank;
import discordbot.db.model.OGuild;
import discordbot.db.model.OUserRank;
import discordbot.main.Config;
import discordbot.main.GuildCheckResult;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.MiscUtil;
import net.dv8tion.jda.utils.PermissionUtil;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages permissions/bans for discord
 */
public class SecurityHandler {
	private static HashSet<String> bannedGuilds;
	private static HashSet<String> bannedUsers;
	private static HashSet<String> contributors;
	private static HashSet<String> botAdmins;

	public SecurityHandler() {
	}

	public static synchronized void initialize() {
		bannedGuilds = new HashSet<>();
		bannedUsers = new HashSet<>();
		contributors = new HashSet<>();
		botAdmins = new HashSet<>();

		List<OGuild> bannedList = CGuild.getBannedGuilds();
		bannedGuilds.addAll(bannedList.stream().map(guild -> guild.discord_id).collect(Collectors.toList()));

		List<OUserRank> contributor = CUserRank.getUsersWith(CRank.findBy("CONTRIBUTOR").id);
		List<OUserRank> bot_admin = CUserRank.getUsersWith(CRank.findBy("BOT_ADMIN").id);
		contributors.addAll(contributor.stream().map(oUserRank -> CUser.getCachedDiscordId(oUserRank.userId)).collect(Collectors.toList()));
		botAdmins.addAll(bot_admin.stream().map(oUserRank -> CUser.getCachedDiscordId(oUserRank.userId)).collect(Collectors.toList()));
	}

	public boolean isBanned(Guild guild) {
		return isGuildBanned(guild.getId());
	}

	public boolean isGuildBanned(String discordId) {
		return bannedGuilds.contains(discordId);
	}

	public SimpleRank getSimpleRank(User user) {
		return getSimpleRankForGuild(user, null);
	}

	public SimpleRank getSimpleRank(User user, MessageChannel channel) {
		if (channel instanceof TextChannel) {
			return getSimpleRankForGuild(user, ((TextChannel) channel).getGuild());
		}
		return getSimpleRankForGuild(user, null);
	}

	/**
	 * Try and figure out what type of guild it is
	 *
	 * @param guild the guild to check
	 * @return what category the guild is labeled as
	 */
	public GuildCheckResult checkGuild(Guild guild) {

		int bots = 0;
		int users = 0;
		if (MiscUtil.getCreationTime(guild.getOwner().getId()).isBefore(OffsetDateTime.now().minusDays(Config.GUILD_OWNER_MIN_ACCOUNT_AGE))) {
			return GuildCheckResult.OWNER_TOO_NEW;
		}
		for (User user : guild.getUsers()) {
			if (user.isBot()) {
				bots++;
			}
			users++;
		}
		if ((double) bots / users > Config.GUILD_MAX_USER_BOT_RATIO) {
			return GuildCheckResult.BOT_GUILD;
		}
		if (users < Config.GUILD_MIN_USERS) {
			return GuildCheckResult.TEST_GUILD;
		}
		return GuildCheckResult.OKE;
	}

	public SimpleRank getSimpleRankForGuild(User user, Guild guild) {
		if (user.getId().equals(Config.CREATOR_ID)) {
			return SimpleRank.CREATOR;
		}
		if (guild == null && user.isBot()) {
			return SimpleRank.BOT;
		}
		if (botAdmins.contains(user.getId())) {
			return SimpleRank.BOT_ADMIN;
		}
		if (contributors.contains(user.getId())) {
			return SimpleRank.CONTRIBUTOR;
		}
		if (bannedUsers.contains(user.getId())) {
			return SimpleRank.BANNED_USER;
		}
		if (guild != null) {
			if (guild.getOwner().equals(user)) {
				return SimpleRank.GUILD_OWNER;
			}
			if (PermissionUtil.checkPermission(guild, user, Permission.ADMINISTRATOR)) {
				return SimpleRank.GUILD_ADMIN;
			}
			if (user.isBot()) {
				return SimpleRank.BOT;
			}
		}
		return SimpleRank.USER;
	}
}
