package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.guildsettings.music.SettingMusicSkipAdminOnly;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.entities.*;

/**
 * !skip
 * skips current active track
 */
public class SkipTrack extends AbstractCommand {
	public SkipTrack() {
		super();
	}

	@Override
	public String getDescription() {
		return "skip current track";
	}

	@Override
	public String getCommand() {
		return "skip";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"skip                  //skips current track",
				"skip adminonly        //check what skipmode its set on",
				"skip adminonly toggle //toggle the skipmode",
				"skip force            //admin-only, force a skip"
//				"skip perm //skips permanently; never hear this song again"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"next"
		};
	}

	private boolean isInVoiceWith(Guild guild, User author) {
		VoiceChannel channel = guild.getVoiceStatusOfUser(author).getChannel();
		if (channel == null) {
			return false;
		}
		for (User user : channel.getUsers()) {
			if (user.getId().equals(guild.getJDA().getSelfInfo().getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
		SimpleRank userRank = bot.security.getSimpleRank(author, channel);
		boolean adminOnly = "true".equals(GuildSettings.getFor(channel, SettingMusicSkipAdminOnly.class));
		if (adminOnly && !userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get(channel, "music_skip_admin_only");
		}
		if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
			return Template.get(channel, "music_required_role_not_found", GuildSettings.getFor(channel, SettingMusicRole.class));
		}
		if (!player.isPlaying()) {
			return Template.get("command_currentlyplaying_nosong");
		}
		if (!isInVoiceWith(guild, author)) {
			return Template.get("music_not_same_voicechannel");
		}
		if (args.length >= 1) {
			switch (args[0]) {
				case "force":
					if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
						player.forceSkip();
						return "";
					}
					return Template.get("music_skip_admin_only");
				case "perm":
				case "permanent":
					return Template.get("command_skip_permanent_success");
				case "admin":
				case "adminonly":
					if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN) && args.length > 1 && args[1].equalsIgnoreCase("toggle")) {
						GuildSettings.get(guild).set(SettingMusicSkipAdminOnly.class, adminOnly ? "false" : "true");
						adminOnly = !adminOnly;
					}
					return Template.get("music_skip_mode", adminOnly ? "admin-only" : "normal");
				default:
					return Template.get("command_invalid_usage");
			}
		}
		if (player.getRequiredVotes() == 1) {
			player.forceSkip();
			return "";
		}
		boolean voteRegistered = player.voteSkip(author);
		if (player.getVoteCount() >= player.getRequiredVotes()) {
			player.forceSkip();
			return Template.get("command_skip_song_skipped");
		}
		if (voteRegistered) {
			return Template.get("command_skip_vote_success", player.getVoteCount(), player.getRequiredVotes());
		}
		return Template.get("command_skip_vote_failed");
	}
}
