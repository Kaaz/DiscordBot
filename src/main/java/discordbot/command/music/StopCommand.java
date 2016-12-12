package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !stop
 * make the bot stop playing music
 */
public class StopCommand extends AbstractCommand {
	public StopCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "stops playing music";
	}

	@Override
	public String getCommand() {
		return "stop";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"leave"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		SimpleRank userRank = bot.security.getSimpleRank(author, channel);
		if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
			return Template.get(channel, "music_required_role_not_found", GuildSettings.getFor(channel, SettingMusicRole.class));
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
		if (userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			player.leave();
			return Template.get("command_stop_success");
		}
		if (!player.isPlaying()) {
			return Template.get("command_currentlyplaying_nosong");
		}
		if (player.isConnected()) {
			if (!player.canUseVoiceCommands(author, userRank)) {
				return Template.get("music_not_same_voicechannel");
			}
			player.leave();
			return Template.get("command_stop_success");
		}
		return Template.get("command_currentlyplaying_nosong");

	}
}