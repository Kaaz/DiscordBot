package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OGuildMember;
import discordbot.db.table.TGuildMember;
import discordbot.guildsettings.defaults.SettingPMUserEvents;
import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.guildsettings.defaults.SettingWelcomeNewUsers;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.role.RoleRankings;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Timestamp;

/**
 * A user joins a guild
 */
public class UserJoinListener extends AbstractEventListener<UserJoinEvent> {
	public UserJoinListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(UserJoinEvent event) {
		IUser user = event.getUser();
		IGuild guild = event.getGuild();
		GuildSettings settings = GuildSettings.get(guild);
		OGuildMember guildMember = TGuildMember.findBy(guild.getID(), user.getID());
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		TGuildMember.insertOrUpdate(guildMember);

		if ("true".equals(settings.getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner(), String.format("[user-event] **%s#%s** joined the guild **%s**", user.getName(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(settings.getOrDefault(SettingWelcomeNewUsers.class))) {
			discordBot.out.sendAsyncMessage(discordBot.getDefaultChannel(guild), String.format(Template.get("welcome_new_user"), user.mention()), null);
		}
		if ("true".equals(settings.getOrDefault(SettingRoleTimeRanks.class))) {
			RoleRankings.assignUserRole(discordBot, guild, user);
		}

	}
}