package novaz.event;

import novaz.core.AbstractEventListener;
import novaz.db.model.RServer;
import novaz.db.model.RUser;
import novaz.db.table.TServers;
import novaz.db.table.TUser;
import novaz.main.NovaBot;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * Whenever the bot joins a discord server
 */
public class JoinServerListener extends AbstractEventListener<GuildCreateEvent> {
	public JoinServerListener(NovaBot novaBot) {
		super(novaBot);
	}

	@Override
	public boolean listenerActivated() {
		return true;
	}

	@Override
	public void handle(GuildCreateEvent event) {

		System.out.println("[event] BOT JOINED SERVER!");
		IGuild guild = event.getGuild();
		List<IChannel> channels = guild.getChannels();
		System.out.println(guild.getID() + guild.getName());
		IUser owner = guild.getOwner();

		RUser user = TUser.findBy(owner.getID());
		user.discord_id = owner.getID();
		user.name = owner.getName();
		TUser.update(user);
		RServer server = TServers.findBy(guild.getID());
		server.discord_id = guild.getID();
		server.name = guild.getName();
		server.owner = user.id;
		TServers.update(server);

		System.out.println(String.format("OWNED: %s %s", owner.getID(), owner.getName()));
		System.out.println("CHANNELS ON HERE:::");
		for (IChannel c : channels) {
			System.out.println(String.format("-> %20s %s", c.getID(), c.getName()));
		}

	}

}