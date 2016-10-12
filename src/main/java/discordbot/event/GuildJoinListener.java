package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.db.model.OGuild;
import discordbot.db.model.OUser;
import discordbot.db.table.TGuild;
import discordbot.db.table.TUser;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.guildsettings.defaults.SettingMusicVolume;
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
		OUser user = TUser.findBy(owner.getID());
		user.discord_id = owner.getID();
		user.name = owner.getName();
		TUser.update(user);
		OGuild server = TGuild.findBy(guild.getID());
		server.discord_id = guild.getID();
		server.name = guild.getName();
		server.owner = user.id;
		if (server.id == 0) {
			TGuild.insert(server);
		}
		discordBot.setVolume(guild, Float.parseFloat(GuildSettings.get(guild).getOrDefault(SettingMusicVolume.class)) / 100F);
		String cmdPre = GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class);
		if (server.active != 1) {
			discordBot.out.sendMessageToCreator(String.format("[**event**] [**guild**] I have just **joined** **%s** (discord-id = %s)", guild.getName(), guild.getID()));

			String message = "Thanks for adding me to your guild!" + Config.EOL +
					"To see what I can do you can type the command `" + cmdPre + "help`." + Config.EOL +
					"Most of my features are opt-in, which means that you'll have to enable them first. Admins can use `" + cmdPre + "config` to change my settings." + Config.EOL +
					"If you need help or would like to give feedback, feel free to let me know on either `" + cmdPre + "discord` or `" + cmdPre + "github`";
			IChannel outChannel = null;
			for (IChannel channel : guild.getChannels()) {
				if (channel.getModifiedPermissions(discordBot.client.getOurUser()).contains(Permissions.SEND_MESSAGES)) {
					outChannel = channel;
					break;
				}
			}
			if (outChannel != null) {
				discordBot.out.sendMessage(outChannel, message);
			} else {
				discordBot.out.sendPrivateMessage(owner, message);
			}
			server.active = 1;
		}
		TGuild.update(server);
		DiscordBot.LOGGER.info("[event] JOINED SERVER! " + guild.getName());
	}

}
