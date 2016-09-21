package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OServer;
import discordbot.db.model.OUser;
import discordbot.db.table.TServers;
import discordbot.db.table.TUser;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Whenever the bot joins a discord server
 */
public class JoinServerListener extends AbstractEventListener<GuildCreateEvent> {
	public JoinServerListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(GuildCreateEvent event) {

		IGuild guild = event.getGuild();
		IUser owner = guild.getOwner();

		discordBot.setVolume(guild, 0.05F);
		OUser user = TUser.findBy(owner.getID());
		user.discord_id = owner.getID();
		user.name = owner.getName();
		TUser.update(user);
		OServer server = TServers.findBy(guild.getID());
		server.discord_id = guild.getID();
		server.name = guild.getName();
		server.owner = user.id;
		TServers.update(server);
		DiscordBot.LOGGER.info("[event] JOINED SERVER! " + guild.getName());
	}

}