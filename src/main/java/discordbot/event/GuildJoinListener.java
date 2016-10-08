package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OServer;
import discordbot.db.model.OUser;
import discordbot.db.table.TServers;
import discordbot.db.table.TUser;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.handler.GuildSettings;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Whenever the bot joins a discord server
 */
public class GuildJoinListener extends AbstractEventListener<GuildCreateEvent> {
	public GuildJoinListener(DiscordBot discordBot) {
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
		String cmdPre = GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class);
		if (server.active == 0) {
			discordBot.out.sendMessageToCreator(String.format("[**event**] [**guild**] I have just **joined** **%s** (discord-id = %s)", guild.getName(), guild.getID()));

			String message = "Thanks for adding me to your guild!" + Config.EOL +
					"To see what I can do you can type the command `" + cmdPre + "help`." + Config.EOL +
					"Most of my features are opt-in, which means that you'll have to enable them first. Admins can use `" + cmdPre + "config` to change my settings." + Config.EOL +
					"If you need help or would like to give feedback, feel free to let me know on either `" + cmdPre + "discord` or `" + cmdPre + "github`";
			IChannel channel = guild.getChannels().get(0);
			if (channel != null && channel.getModifiedPermissions(discordBot.client.getOurUser()).contains(Permissions.SEND_MESSAGES)) {
				discordBot.out.sendMessage(channel, message);
			} else {
				discordBot.out.sendPrivateMessage(owner, message);
			}
			server.active = 1;
		}
		TServers.update(server);
		DiscordBot.LOGGER.info("[event] JOINED SERVER! " + guild.getName());
	}

}