package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.defaults.SettingMusicVolume;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !volume [vol]
 * sets the volume of the music player
 * With no params returns the current volume
 */
public class Volume extends AbstractCommand {
	public Volume(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			float volume;
			try {
				volume = Float.parseFloat(args[0]);
				if (volume > 0 && volume <= 100) {
					bot.setVolume(channel.getGuild(), volume / 100F);
					GuildSettings.get(channel.getGuild()).set(SettingMusicVolume.class, String.valueOf((int) (bot.getVolume(channel.getGuild()) * 100F)));
					return Template.get("command_volume_changed") + " (now " + ((int) (bot.getVolume(channel.getGuild()) * 100F)) + "%)";
				}
			} catch (NumberFormatException ignored) {
			}
			return Template.get("command_volume_invalid_parameters");
		}
		return "Current volume: " + bot.getVolume(channel.getGuild()) * 100 + "%";
	}
}
