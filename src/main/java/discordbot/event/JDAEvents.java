package discordbot.event;

import discordbot.core.ExitCode;
import discordbot.db.model.OGuild;
import discordbot.db.model.OGuildMember;
import discordbot.db.model.OUser;
import discordbot.db.table.TBotEvent;
import discordbot.db.table.TGuild;
import discordbot.db.table.TGuildMember;
import discordbot.db.table.TUser;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.guildsettings.defaults.SettingPMUserEvents;
import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.guildsettings.defaults.SettingWelcomeNewUsers;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.role.RoleRankings;
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.events.DisconnectEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.events.voice.VoiceJoinEvent;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.managers.AudioManager;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created on 12-10-2016
 */
public class JDAEvents extends ListenerAdapter {
	final private String[] specialGuilds = new String[]{
			"225168913808228352",
			"180818466847064065"
	};
	private DiscordBot discordBot;

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
		String cmdPre = GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class);
		if (server.active != 1) {
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
			TBotEvent.insert("GUILD", "JOIN", String.format(" %s [dis-id: %s][iid: %s]", guild.getName(), guild.getId(), server.id));
			discordBot.getContainer().guildJoined();
			discordBot.out.sendMessageToCreator(String.format("[**event**] [**guild**] I have just **joined** **%s** (discord-id = %s)", guild.getName(), guild.getId()));
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
		discordBot.getContainer().guildLeft();
		TBotEvent.insert("GUILD", "LEAVE", String.format(" %s [dis-id: %s][iid: %s]", guild.getName(), guild.getId(), server.id));
		discordBot.out.sendMessageToCreator(String.format("[**event**] [**guild**] I have been **kicked** from **%s** (discord-id = %s)", server.name, server.discord_id));
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		super.onGuildMessageReceived(event);
		discordBot.handleMessage(event.getGuild(), event.getChannel(), event.getAuthor(), event.getMessage());
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		discordBot.handlePrivateMessage(event.getChannel(), event.getAuthor(), event.getMessage());
	}

	@Override
	public void onReady(ReadyEvent event) {
		discordBot.markReady(true);
		System.out.println("[event] Bot is ready!");
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		GuildSettings settings = GuildSettings.get(guild);
		OGuildMember guildMember = TGuildMember.findBy(guild.getId(), user.getId());
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		TGuildMember.insertOrUpdate(guildMember);

		if ("true".equals(settings.getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner(), String.format("[user-event] **%s#%s** joined the guild **%s**", user.getUsername(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(settings.getOrDefault(SettingWelcomeNewUsers.class))) {
			discordBot.out.sendAsyncMessage(discordBot.getDefaultChannel(guild), String.format(Template.get("welcome_new_user"), user.getAsMention()), null);
		}
		if ("true".equals(settings.getOrDefault(SettingRoleTimeRanks.class))) {
			RoleRankings.assignUserRole(discordBot, guild, user);
		}
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner(), String.format("[user-event] **%s#%s** left the guild **%s**", user.getUsername(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingWelcomeNewUsers.class))) {
			discordBot.out.sendAsyncMessage(guild.getTextChannels().get(0), String.format(Template.get("message_user_leaves"), user.getAsMention()), null);
		}
		OGuildMember guildMember = TGuildMember.findBy(guild.getId(), user.getId());
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		TGuildMember.insertOrUpdate(guildMember);
	}

	@Override
	public void onUserGameUpdate(UserGameUpdateEvent event) {
		if (!Config.BOT_ON_BOT_ACTION) {
			return;
		}
		User user = event.getUser();
		if (!user.isBot() || user.getId().equals(event.getJDA().getSelfInfo().getId())) {
			return;
		}
		Game status = user.getCurrentGame();
		if (status == null || status.getName() == null || event.getJDA().getSelfInfo().getCurrentGame() == null) {
			return;
		}
		if (status.getName().equals(event.getJDA().getSelfInfo().getCurrentGame().getName())) {
			for (String specialGuild : specialGuilds) {
				Guild guild = event.getJDA().getGuildById(specialGuild);
				if (guild == null) {
					continue;
				}
				User guildUser = guild.getUserById(user.getId());
				if (guildUser != null) {
					TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
					MessageHistory history = defaultChannel.getHistory();
					List<Message> retrieve = history.retrieve(25);
					if (retrieve == null) {
						return;
					}
					for (Message message : retrieve) {
						if (message.getAuthor().getId().equals(event.getJDA().getSelfInfo().getId())) {
							return;
						}
					}
					discordBot.out.sendAsyncMessage(defaultChannel, "Oh hey look " + user.getAsMention() + " we have the same status :joy:", null);
				}

			}
		}
	}

	@Override
	public void onVoiceJoin(VoiceJoinEvent event) {
		super.onVoiceJoin(event);
	}


	@Override
	public void onVoiceLeave(VoiceLeaveEvent event) {
		VoiceChannel channel = event.getOldChannel();
		AudioManager audioManager = channel.getGuild().getAudioManager();
		VoiceChannel connectedVoice = audioManager.getConnectedChannel();
		if (connectedVoice == null || !channel.getId().equals(connectedVoice.getId())) {
			return;
		}
		boolean shouldLeave = true;
		for (User user : connectedVoice.getUsers()) {
			if (!user.isBot()) {
				shouldLeave = false;
				break;
			}
		}
		if (shouldLeave) {
			audioManager.closeAudioConnection();
			discordBot.out.sendAsyncMessage(discordBot.getMusicChannel(channel.getGuild()), Template.get("music_no_one_listens_i_leave"), null);
		}
	}
}
