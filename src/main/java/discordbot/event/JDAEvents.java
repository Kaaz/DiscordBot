package discordbot.event;

import discordbot.core.ExitCode;
import discordbot.db.model.OGuild;
import discordbot.db.model.OUser;
import discordbot.db.table.TGuild;
import discordbot.db.table.TUser;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.handler.GuildSettings;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.DisconnectEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

/**
 * Created on 12-10-2016
 */
public class JDAEvents extends ListenerAdapter {
	DiscordBot discordBot;

	public JDAEvents(DiscordBot bot) {
		this.discordBot = bot;
	}

	public void onDisconnect(DisconnectEvent event) {
		DiscordBot.LOGGER.info("[event] DISCONNECTED! ");
		Launcher.stop(ExitCode.DISCONNECTED);
	}

	public void onGuildJoin(GuildJoinEvent event) {
		Guild guild = event.getGuild();
		User owner = guild.getOwner();
		OUser user = TUser.findBy(owner.getId());
		user.discord_id = owner.getId();
		user.name = owner.getUsername();
		TUser.update(user);
		OGuild server = TGuild.findBy(guild.getId());
		server.discord_id = guild.getId();
		server.name = guild.getName();
		server.owner = user.id;
		if (server.id == 0) {
			TGuild.insert(server);
		}
		// @todo set volume per guild on startup
//		discordBot.setVolume(guild, Float.parseFloat(GuildSettings.get(guild).getOrDefault(SettingMusicVolume.class)) / 100F);
		String cmdPre = GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class);
		if (server.active != 1) {
			discordBot.out.sendMessageToCreator(String.format("[**event**] [**guild**] I have just **joined** **%s** (discord-id = %s)", guild.getName(), guild.getId()));

			String message = "Thanks for adding me to your guild!" + Config.EOL +
					"To see what I can do you can type the command `" + cmdPre + "help`." + Config.EOL +
					"Most of my features are opt-in, which means that you'll have to enable them first. Admins can use `" + cmdPre + "config` to change my settings." + Config.EOL +
					"If you need help or would like to give feedback, feel free to let me know on either `" + cmdPre + "discord` or `" + cmdPre + "github`";
			TextChannel outChannel = null;
			for (TextChannel channel : guild.getTextChannels()) {
				if (channel.checkPermission(event.getJDA().getSelfInfo(), Permission.MESSAGE_WRITE)) {
					outChannel = channel;
					break;
				}
			}
			if (outChannel != null) {
				discordBot.out.sendAsyncMessage(outChannel, message, null);
			} else {
				discordBot.out.sendPrivateMessage(owner, message);
			}
			server.active = 1;
		}
		TGuild.update(server);
		DiscordBot.LOGGER.info("[event] JOINED SERVER! " + guild.getName());
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		Guild guild = event.getGuild();
		OGuild server = TGuild.findBy(guild.getId());
		server.active = 0;
		TGuild.update(server);
		discordBot.out.sendMessageToCreator(String.format("[**event**] [**guild**] I have been **kicked** from **%s** (discord-id = %s)", guild.getName(), guild.getId()));
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		super.onGuildMessageReceived(event);
		discordBot.handleMessage(event.getGuild(), event.getChannel(), event.getAuthor(), event.getMessage());
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		discordBot.handlePrivateMessage(event.getChannel(), event.getAuthor(), event.getMessage());
	}

	@Override
	public void onReady(ReadyEvent event) {
		discordBot.markReady(true);
		System.out.println("[event] Bot is ready!");
	}
}
