package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OGuildMember;
import discordbot.db.table.TGuildMember;
import discordbot.guildsettings.defaults.SettingPMUserEvents;
import discordbot.guildsettings.defaults.SettingWelcomeNewUsers;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Timestamp;

/**
 * A user joins a guild
 */
public class UserLeaveListener extends AbstractEventListener<UserLeaveEvent> {
	public UserLeaveListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(UserLeaveEvent event) {
		IUser user = event.getUser();
		IGuild guild = event.getGuild();
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner(), String.format("[user-event] **%s#%s** left the guild **%s**", user.getName(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingWelcomeNewUsers.class))) {
			discordBot.out.sendMessage(guild.getChannels().get(0), String.format(Template.get("message_user_leaves"), user.mention()));
		}
		OGuildMember guildMember = TGuildMember.findBy(guild.getID(), user.getID());
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		TGuildMember.insertOrUpdate(guildMember);

	}
}