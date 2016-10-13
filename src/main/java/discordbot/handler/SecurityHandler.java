package discordbot.handler;

import discordbot.db.model.OGuild;
import discordbot.db.table.TGuild;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.Guild;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages permissions/bans for discord
 */
public class SecurityHandler {
	private final DiscordBot discordBot;
	private HashSet<String> bannedGuilds;

	public SecurityHandler(DiscordBot discordBot) {

		this.discordBot = discordBot;
		loadBanLists();
	}

	public boolean isBanned(Guild guild) {
		return isGuildBanned(guild.getId());
	}

	public boolean isGuildBanned(String discordId) {
		return bannedGuilds.contains(discordId);
	}

	private void loadBanLists() {
		bannedGuilds = new HashSet<>();
		List<OGuild> bannedList = TGuild.getBannedGuilds();
		bannedGuilds.addAll(bannedList.stream().map(guild -> guild.discord_id).collect(Collectors.toList()));
	}
}
