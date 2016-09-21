package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OGuildMember;
import discordbot.db.table.TGuildMember;
import discordbot.guildsettings.defaults.SettingPMUserEvents;
import discordbot.guildsettings.defaults.SettingWelcomeNewUsers;
import discordbot.handler.GuildSettings;
import discordbot.handler.TextHandler;
import discordbot.main.DiscordBot;
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
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner(), String.format("[user-event] **%s** joined the guild **%s**", user.mention(), guild.getName()));
		}
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingWelcomeNewUsers.class))) {
			discordBot.out.sendMessage(guild.getChannels().get(0), String.format(TextHandler.get("welcome_new_user"), user.mention()));
		}
		OGuildMember guildMember = TGuildMember.findBy(guild.getID(), user.getID());
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		TGuildMember.insertOrUpdate(guildMember);

	}
}