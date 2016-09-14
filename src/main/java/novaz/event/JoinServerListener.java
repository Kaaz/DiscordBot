package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.db.model.OServer;
import novaz.db.model.OUser;
import novaz.db.table.TServers;
import novaz.db.table.TUser;
import novaz.main.NovaBot;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Whenever the bot joins a discord server
 */
public class JoinServerListener extends AbstractEventListener<GuildCreateEvent> {
	public JoinServerListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(GuildCreateEvent event) {

		IGuild guild = event.getGuild();
		IUser owner = guild.getOwner();

		novaBot.setVolume(guild, 0.05F);
		OUser user = TUser.findBy(owner.getID());
		user.discord_id = owner.getID();
		user.name = owner.getName();
		TUser.update(user);
		OServer server = TServers.findBy(guild.getID());
		server.discord_id = guild.getID();
		server.name = guild.getName();
		server.owner = user.id;
		TServers.update(server);
		NovaBot.LOGGER.info("[event] JOINED SERVER! " + guild.getName());
	}

}