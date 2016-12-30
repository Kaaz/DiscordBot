package discordbot.event;

import com.vdurmont.emoji.EmojiParser;
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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * Created on 12-10-2016
 */
public class JDAEvents extends ListenerAdapter {
	private final DiscordBot discordBot;

	public JDAEvents(DiscordBot bot) {
		this.discordBot = bot;
	}

	public void onDisconnect(DisconnectEvent event) {
		DiscordBot.LOGGER.info("[event] DISCONNECTED! ");
	}

	@Override
	public void onStatusChange(StatusChangeEvent event) {
		discordBot.getContainer().reportStatus(event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardId() : 0, event.getOldStatus(), event.getStatus());
	}

	@Override
	public void onResume(ResumedEvent event) {
		super.onResume(event);
	}

	@Override
	public void onReconnect(ReconnectedEvent event) {
	}

	public void onGuildJoin(GuildJoinEvent event) {
		Guild guild = event.getGuild();
		User owner = guild.getOwner().getUser();
		OUser user = CUser.findBy(owner.getId());
		user.discord_id = owner.getId();
		user.name = EmojiParser.parseToAliases(owner.getName());
		CUser.update(user);
		OGuild server = CGuild.findBy(guild.getId());
		server.discord_id = guild.getId();
		server.name = EmojiParser.parseToAliases(guild.getName());
		server.owner = user.id;
		if (server.id == 0) {
			CGuild.insert(server);
		}
		if (server.isBanned()) {
			guild.leave().queue();
			return;
		}
		discordBot.loadGuild(guild);
		String cmdPre = GuildSettings.get(guild).getOrDefault(SettingCommandPrefix.class);
		GuildCheckResult guildCheck = discordBot.security.checkGuild(guild);
		if (server.active != 1) {
			String message = "Thanks for adding me to your guild!" + Config.EOL +
					"To see what I can do you can type the command `" + cmdPre + "help`." + Config.EOL +
					"Most of my features are opt-in, which means that you'll have to enable them first. Admins can use `" + cmdPre + "config` to change my settings." + Config.EOL +
					"Most commands has a help portion which can be accessed by typing help after the command; For instance: `" + cmdPre + "`skip help" +
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
				if (channel.canTalk()) {
					outChannel = channel;
					break;
				}
			}
			CBotEvent.insert(":house:", ":white_check_mark:",
					String.format(":id: %s | :hash: %s | :busts_in_silhouette: %s | %s",
							guild.getId(),
							server.id,
							guild.getMembers().size(),
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
				guild.leave().queue();
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
		discordBot.sendStatsToDiscordPw();
		Launcher.log("bot leaves guild", "bot", "guild-leave",
				"guild-id", guild.getId(),
				"guild-name", guild.getName());
		CBotEvent.insert(":house_abandoned:", ":fire:",
				String.format(":id: %s | :hash: %s | %s",
						guild.getId(),
						server.id,
						EmojiParser.parseToAliases(guild.getName())
				));
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		handleReaction(event, true);
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		handleReaction(event, false);
	}

	private void handleReaction(GenericMessageReactionEvent e, boolean adding) {
		if (e.getUser().isBot()) {
			return;
		}
		if (!(e.getChannel() instanceof TextChannel)) {
			return;
		}
		discordBot.musicReactionHandler.handle(e.getMessageId(), (TextChannel) e.getChannel(), e.getUser(), e.getReaction().getEmote(), adding);
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
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
	public void onGuildBan(GuildBanEvent event) {
		discordBot.logGuildEvent(event.getGuild(), "\uD83D\uDED1", "**" + event.getUser().getName() + "#" + event.getUser().getDiscriminator() + "** has been banned");
	}

	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		String message = "**" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator() + "** changed nickname ";
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
		User user = event.getMember().getUser();
		Guild guild = event.getGuild();
		GuildSettings settings = GuildSettings.get(guild);
		OGuildMember guildMember = CGuildMember.findBy(guild.getId(), user.getId());
		boolean firstTime = guildMember.joinDate == null;
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		CGuildMember.insertOrUpdate(guildMember);

		if ("true".equals(settings.getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner().getUser(), String.format("[user-event] **%s#%s** joined the guild **%s**", user.getName(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(settings.getOrDefault(SettingWelcomeNewUsers.class))) {
			TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
			if (defaultChannel == null) {
				GuildSettings.get(guild.getId()).set(guild, SettingWelcomeNewUsers.class, "false");
				return;
			}
			defaultChannel.sendMessage(
					Template.getWithTags(defaultChannel, firstTime ? "welcome_new_user" : "welcome_back_user", user)).queue(
					message -> discordBot.schedule(() -> discordBot.out.saveDelete(message), Config.DELETE_MESSAGES_AFTER * 5, TimeUnit.MILLISECONDS)
			);
		}

		Launcher.log("user joins guild", "guild", "member-join",
				"guild-id", guild.getId(),
				"guild-name", guild.getName(),
				"user-id", user.getId(),
				"user-name", user.getName());

		if ("true".equals(settings.getOrDefault(SettingRoleTimeRanks.class)) && !user.isBot()) {
			RoleRankings.assignUserRole(discordBot, guild, user);
		}
		discordBot.logGuildEvent(guild, "\uD83D\uDC64", "**" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator() + "** joined the guild");
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		User user = event.getMember().getUser();
		if (user.isBot()) {
			return;
		}
		Guild guild = event.getGuild();
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingPMUserEvents.class))) {
			discordBot.out.sendPrivateMessage(guild.getOwner().getUser(), String.format("[user-event] **%s#%s** left the guild **%s**", user.getName(), user.getDiscriminator(), guild.getName()));
		}
		if ("true".equals(GuildSettings.get(guild).getOrDefault(SettingWelcomeNewUsers.class))) {
			TextChannel defaultChannel = discordBot.getDefaultChannel(guild);
			if (defaultChannel != null) {
				defaultChannel.sendMessage(
						Template.getWithTags(defaultChannel, "message_user_leaves", user)).queue(
						message -> discordBot.schedule(() -> discordBot.out.saveDelete(message), Config.DELETE_MESSAGES_AFTER * 5, TimeUnit.MILLISECONDS)
				);
			}
		}
		Launcher.log("user leaves guild", "guild", "member-leave",
				"guild-id", guild.getId(),
				"guild-name", guild.getName(),
				"user-id", user.getId(),
				"user-name", user.getName());
		OGuildMember guildMember = CGuildMember.findBy(guild.getId(), user.getId());
		guildMember.joinDate = new Timestamp(System.currentTimeMillis());
		CGuildMember.insertOrUpdate(guildMember);
		discordBot.logGuildEvent(guild, "\uD83C\uDFC3", "**" + user.getName() + "#" + user.getDiscriminator() + "** left the guild");
	}

	@Override
	public void onUserGameUpdate(UserGameUpdateEvent event) {
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		if (event.getMember().getUser().isBot()) {
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
		if (event.getChannelJoined().getId().equals(autoChannel) || event.getChannelJoined().getName().equalsIgnoreCase(autoChannel)) {
			player.connectTo(event.getChannelJoined());
			player.playRandomSong();
		}
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		checkLeaving(event.getGuild(), event.getChannelLeft(), event.getMember().getUser());
		onGuildVoiceJoin(new GuildVoiceJoinEvent(event.getJDA(), 0, event.getMember()));
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		checkLeaving(event.getGuild(), event.getChannelLeft(), event.getMember().getUser());
	}

	private void checkLeaving(Guild guild, VoiceChannel channel, User user) {
		if (user.isBot()) {
			return;
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, discordBot);
		if (!player.isConnected()) {
			return;
		}
		if (!player.isConnectedTo(channel)) {
			return;
		}
		player.unregisterVoteSkip(user);
		if (player.getVoteCount() >= player.getRequiredVotes()) {
			player.forceSkip();
		}
		for (Member member : guild.getAudioManager().getConnectedChannel().getMembers()) {
			if (!member.getUser().isBot()) {
				return;
			}
		}
		player.leave();
		String autoChannel = GuildSettings.get(guild).getOrDefault(SettingMusicAutoVoiceChannel.class);
		if (!"false".equalsIgnoreCase(autoChannel) && channel.getName().equalsIgnoreCase(autoChannel)) {
			return;
		}
		TextChannel musicChannel = discordBot.getMusicChannel(guild);
		if (musicChannel != null && musicChannel.canTalk()) {
			discordBot.out.sendAsyncMessage(musicChannel, Template.get("music_no_one_listens_i_leave"));
		}
	}
}