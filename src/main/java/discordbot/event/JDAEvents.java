package discordbot.event;

import com.vdurmont.emoji.EmojiParser;
import discordbot.core.ExitCode;
import discordbot.db.controllers.CBotEvent;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CGuildMember;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OGuild;
import discordbot.db.model.OGuildMember;
import discordbot.db.model.OUser;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.guildsettings.defaults.SettingPMUserEvents;
import discordbot.guildsettings.defaults.SettingRoleTimeRanks;
import discordbot.guildsettings.defaults.SettingWelcomeNewUsers;
import discordbot.guildsettings.music.SettingMusicAutoVoiceChannel;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.GuildCheckResult;
import discordbot.main.Launcher;
import discordbot.role.RoleRankings;
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.events.DisconnectEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberBanEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.events.voice.VoiceJoinEvent;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import java.sql.Timestamp;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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
		OUser user = CUser.findBy(owner.getId());
		user.discord_id = owner.getId();
		user.name = EmojiParser.parseToAliases(owner.getUsername());
		CUser.update(user);
		OGuild server = CGuild.findBy(guild.getId());
		server.discord_id = guild.getId();
		server.name = EmojiParser.parseToAliases(guild.getName());
		server.owner = user.id;
		if (server.id == 0) {
			CGuild.insert(server);
		}
		if (server.isBanned()) {
			guild.getManager().leave();
			return;
		}
		discordBot.loadGuild(guild);
		String cmdPre = GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class);
		GuildCheckResult guildCheck = discordBot.security.checkGuild(guild);
		if (server.active != 1) {
			String message = "Thanks for adding me to your guild!" + Config.EOL +
					"To see what I can do you can type the command `" + cmdPre + "help`." + Config.EOL +
					"Most of my features are opt-in, which means that you'll have to enable them first. Admins can use `" + cmdPre + "config` to change my settings." + Config.EOL +
					"If you need help or would like to give feedback, feel free to let me know on either `" + cmdPre + "discord` or `" + cmdPre + "github`";
			switch (guildCheck) {
				case TEST_GUILD:
					message += Config.EOL + Config.EOL + " :warning: The guild has been categorized as a test guild. This means that I might leave this guild when the next cleanup happens." + Config.EOL +
							"If this is not a test guild feel free to join my `" + cmdPre + "discord` and ask to have your guild added to the whitelist!";
					break;
				case BOT_GUILD:
					message += Config.EOL + Config.EOL + ":warning: :robot: Too many bots here, I'm leaving! " + Config.EOL +
							"If your guild is not a collection of bots and you actually plan on using me join my `" + cmdPre + "discord` and ask to have your guild added to the whitelist!";
					break;
				case SMALL:
				case OWNER_TOO_NEW:
				case OKE:
				default:
					break;
			}
			TextChannel outChannel = null;
			for (TextChannel channel : guild.getTextChannels()) {
				if (channel.checkPermission(event.getJDA().getSelfInfo(), Permission.MESSAGE_WRITE)) {
					outChannel = channel;
					break;
				}
			}
			CBotEvent.insert(":house:", ":white_check_mark:",
					String.format(":id: %s | :hash: %s | :busts_in_silhouette: %s | %s",
							guild.getId(),
							server.id,
							guild.getUsers().size(),
							EmojiParser.parseToAliases(guild.getName())));
			discordBot.getContainer().guildJoined();
			Launcher.log("bot joins guild", "bot", "guild-join",
					"guild-id", guild.getId(),
					"guild-name", guild.getName());
			if (outChannel != null) {
				discordBot.out.sendAsyncMessage(outChannel, message, null);
			} else {
				discordBot.out.sendPrivateMessage(owner, message);
			}
			if (guildCheck.equals(GuildCheckResult.BOT_GUILD)) {
				guild.getManager().leave();
			}
			server.active = 1;
		}
		CGuild.update(server);
		DiscordBot.LOGGER.info("[event] JOINED SERVER! " + guild.getName());
		discordBot.sendStatsToDiscordPw();
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		Guild guild = event.getGuild();
		OGuild server = CGuild.findBy(guild.getId());
		server.active = 0;
		CGuild.update(server);
		discordBot.clearGuildData(guild);
		discordBot.getContainer().guildLeft();
		if (server.isBanned()) {
			return;
		}
		CBotEvent.insert(":house_abandoned:", ":fire:",
				String.format(":id: %s | :hash: %s | %s",
						guild.getId(),
						server.id,
						EmojiParser.parseToAliases(guild.getName())
				));
		Launcher.log("bot leaves guild", "bot", "guild-leave",
				"guild-id", guild.getId(),
				"guild-name", guild.getName());
		discordBot.sendStatsToDiscordPw();
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
	public void onGuildMemberBan(GuildMemberBanEvent event) {
		discordBot.logGuildEvent(event.getGuild(), "\uD83D\uDED1", "**" + event.getUser().getUsername() + "#" + event.getUser().getDiscriminator() + "** has been banned");
	}

	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		String message = "**" + event.getUser().getUsername() + "#" + event.getUser().getDiscriminator() + "** changed nickname ";
		if (event.getPrevNick() != null) {
			message += "from _~~" + event.getPrevNick() + "~~_ ";
		}
		if (event.getNewNick() != null) {
			message += "to **" + event.getNewNick() + "**";
		} else {
			message += "back to normal";
		}
		discordBot.logGuildEvent(event.getGuild(), "\uD83C\uDFF7", message);
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		GuildSettings settings = GuildSettings.get(guild);
		OGuildMember guildMember = CGuildMember.findBy(guild.getId(), user.getId());
		boolean firstTime = guildMember.joinDate == null;
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		CGuildMember.insertOrUpdate(guildMember);

		if ("true".equals(settings.getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner(), String.format("[user-event] **%s#%s** joined the guild **%s**", user.getUsername(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(settings.getOrDefault(SettingWelcomeNewUsers.class))) {
			TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
			defaultChannel.sendMessageAsync(
					Template.getWithTags(defaultChannel, firstTime ? "welcome_new_user" : "welcome_back_user", user),
					message ->
							discordBot.timer.schedule(new TimerTask() {
								@Override
								public void run() {
									if (message != null) {
										message.deleteMessage();
									}
								}
							}, Config.DELETE_MESSAGES_AFTER * 5)
			);
		}
		Launcher.log("user joins guild", "guild", "member-join",
				"guild-id", guild.getId(),
				"guild-name", guild.getName(),
				"user-id", user.getId(),
				"user-name", user.getUsername());

		if ("true".equals(settings.getOrDefault(SettingRoleTimeRanks.class)) && !user.isBot()) {
			RoleRankings.assignUserRole(discordBot, guild, user);
		}
		discordBot.logGuildEvent(guild, "\uD83D\uDC64", "**" + event.getUser().getUsername() + "#" + event.getUser().getDiscriminator() + "** joined the guild");
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		boolean isActuallyBanned = event instanceof GuildMemberBanEvent;
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner(), String.format("[user-event] **%s#%s** left the guild **%s**", user.getUsername(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingWelcomeNewUsers.class))) {
			TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
			defaultChannel.sendMessageAsync(
					Template.getWithTags(defaultChannel, "message_user_leaves", user),
					message ->
							discordBot.timer.schedule(new TimerTask() {
								@Override
								public void run() {
									if (message != null) {
										message.deleteMessage();
									}
								}
							}, Config.DELETE_MESSAGES_AFTER * 5)
			);
		}
		Launcher.log("user leaves guild", "guild", "member-leave",
				"guild-id", guild.getId(),
				"guild-name", guild.getName(),
				"user-id", user.getId(),
				"user-name", user.getUsername());
		OGuildMember guildMember = CGuildMember.findBy(guild.getId(), user.getId());
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		CGuildMember.insertOrUpdate(guildMember);
		if (!isActuallyBanned) {
			discordBot.logGuildEvent(guild, "\uD83C\uDFC3", "**" + event.getUser().getUsername() + "#" + event.getUser().getDiscriminator() + "** left the guild");
		}
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
		if (event.getUser().isBot()) {
			return;
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(event.getGuild(), discordBot);
		if (player.isConnected()) {
			return;
		}
		String autoChannel = GuildSettings.get(event.getGuild()).getOrDefault(SettingMusicAutoVoiceChannel.class);
		if ("false".equalsIgnoreCase(autoChannel)) {
			return;
		}
		if (event.getChannel().getName().equalsIgnoreCase(autoChannel)) {
			player.connectTo(event.getChannel());
			try {
				Thread.sleep(2L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			player.playRandomSong();
		}
	}


	@Override
	public void onVoiceLeave(VoiceLeaveEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(event.getGuild(), discordBot);
		if (!player.isConnected()) {
			return;
		}
		if (!player.isConnectedTo(event.getOldChannel())) {
			return;
		}
		player.unregisterVoteSkip(event.getUser());
		if (player.getVoteCount() >= player.getRequiredVotes()) {
			player.forceSkip();
		}
		for (User user : event.getGuild().getAudioManager().getConnectedChannel().getUsers()) {
			if (!user.isBot()) {
				return;
			}
		}
		discordBot.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				MusicPlayerHandler player = MusicPlayerHandler.getFor(event.getGuild(), discordBot);
				if (!player.isConnected()) {
					return;
				}
				if (!player.isConnectedTo(event.getOldChannel())) {
					return;
				}
				for (User user : event.getGuild().getAudioManager().getConnectedChannel().getUsers()) {
					if (!user.isBot()) {
						return;
					}
				}
				player.leave();
				String autoChannel = GuildSettings.get(event.getGuild()).getOrDefault(SettingMusicAutoVoiceChannel.class);
				if (!"false".equalsIgnoreCase(autoChannel) && event.getOldChannel().getName().equalsIgnoreCase(autoChannel)) {
					return;
				}
				discordBot.out.sendAsyncMessage(discordBot.getMusicChannel(event.getGuild()), Template.get("music_no_one_listens_i_leave"));
			}
		}, TimeUnit.SECONDS.toMillis(30));

	}
}