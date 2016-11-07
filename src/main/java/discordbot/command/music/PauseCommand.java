package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !pause
 * pause the music or resume it
 */
public class PauseCommand extends AbstractCommand {
	public PauseCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "pauses the music or resumes it if its paused";
	}

	@Override
	public String getCommand() {
		return "pause";
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
				"resume"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		SimpleRank userRank = bot.security.getSimpleRank(author, channel);
		if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
			return Template.get(channel, "music_required_role_not_found", GuildSettings.getFor(channel, SettingMusicRole.class));
		}
		if (!MusicPlayerHandler.getFor(guild, bot).canTogglePause()) {
			return Template.get("music_state_not_started");
		}
		if (MusicPlayerHandler.getFor(guild, bot).togglePause()) {
			return Template.get("music_state_paused");
		}
		return Template.get("music_state_resumed");
	}
}