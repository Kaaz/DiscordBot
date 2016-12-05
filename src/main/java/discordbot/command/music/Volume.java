package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.defaults.SettingAdminVolume;
import discordbot.guildsettings.music.SettingMusicVolume;
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
 * !volume [vol]
 * sets the volume of the music player
 * With no params returns the current volume
 */
public class Volume extends AbstractCommand {
	public Volume() {
		super();
	}

	@Override
	public String getDescription() {
		return "gets and sets the volume of the music";
	}

	@Override
	public String getCommand() {
		return "volume";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"volume              //shows current volume",
				"volume <1 to 100>   //sets volume"};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"vol"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		if (args.length > 0) {
			if (GuildSettings.getFor(channel, SettingAdminVolume.class).equals("true") && bot.security.getSimpleRank(author).isAtLeast(SimpleRank.GUILD_ADMIN)) {
				return Template.get("command_volume_invalid_permissions");
			}
			int volume;
			try {
				volume = Integer.parseInt(args[0]);
				MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
				if (volume > 0 && volume <= 100) {
					player.setVolume(volume);
					GuildSettings.get(guild).set(SettingMusicVolume.class, String.valueOf(player.getVolume()));
					return Template.get("command_volume_changed", bot.getVolume(guild));
				}
			} catch (NumberFormatException ignored) {
			}
			return Template.get("command_volume_invalid_parameters");
		}
		return "Current volume: " + bot.getVolume(guild) + "%";
	}
}
