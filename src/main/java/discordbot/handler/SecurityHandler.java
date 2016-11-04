package discordbot.handler;

import discordbot.db.model.OGuild;
import discordbot.db.model.OUserRank;
import discordbot.db.table.TGuild;
import discordbot.db.table.TRank;
import discordbot.db.table.TUser;
import discordbot.db.table.TUserRank;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages permissions/bans for discord
 */
public class SecurityHandler {
	private static HashSet<String> bannedGuilds;
	private static HashSet<String> bannedUsers;
	private static HashSet<String> contributers;
	private static HashSet<String> botAdmins;
	private final DiscordBot discordBot;

	public SecurityHandler(DiscordBot discordBot) {

		this.discordBot = discordBot;
	}

	public static synchronized void initialize() {
		bannedGuilds = new HashSet<>();
		bannedUsers = new HashSet<>();
		contributers = new HashSet<>();
		botAdmins = new HashSet<>();

		List<OGuild> bannedList = TGuild.getBannedGuilds();
		bannedGuilds.addAll(bannedList.stream().map(guild -> guild.discord_id).collect(Collectors.toList()));

		List<OUserRank> contributor = TUserRank.getUsersWith(TRank.findBy("CONTRIBUTOR").id);
		List<OUserRank> bot_admin = TUserRank.getUsersWith(TRank.findBy("BOT_ADMIN").id);
		contributers.addAll(contributor.stream().map(oUserRank -> TUser.getCachedDiscordId(oUserRank.userId)).collect(Collectors.toList()));
		botAdmins.addAll(bot_admin.stream().map(oUserRank -> TUser.getCachedDiscordId(oUserRank.userId)).collect(Collectors.toList()));
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

	public SimpleRank getSimpleRankForGuild(User user, Guild guild) {
		if (discordBot.isCreator(user)) {
			return SimpleRank.CREATOR;
		}
		if (user.isBot()) {
			return SimpleRank.BOT;
		}
		if (botAdmins.contains(user.getId())) {
			return SimpleRank.BOT_ADMIN;
		}
		if (contributers.contains(user.getId())) {
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
		}
		return SimpleRank.USER;
	}
}
