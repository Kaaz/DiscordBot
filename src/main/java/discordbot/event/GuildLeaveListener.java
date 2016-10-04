package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OServer;
import discordbot.db.table.TServers;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.GuildLeaveEvent;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Created on 4-10-2016
 */
public class GuildLeaveListener extends AbstractEventListener<GuildLeaveEvent> {
	public GuildLeaveListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(GuildLeaveEvent event) {
		IGuild guild = event.getGuild();
		OServer server = TServers.findBy(guild.getID());
		server.active = 0;
		TServers.update(server);
		discordBot.out.sendMessageToCreator(String.format("[**event**] [**guild**] I have been **kicked** from **%s** (discord-id = %s)", guild.getName(), guild.getID()));
	}
}