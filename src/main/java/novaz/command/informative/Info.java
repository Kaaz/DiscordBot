package novaz.command.informative;

import novaz.core.AbstractCommand;
import novaz.handler.GuildSettings;
import novaz.handler.guildsettings.defaults.SettingCommandPrefix;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !leave
 * make the bot leave
 */
public class Info extends AbstractCommand {
	public Info(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Shows info about the bot";
	}

	@Override
	public String getCommand() {
		return "info";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		String cmdPrefix = GuildSettings.get(channel.getGuild()).getOrDefault(SettingCommandPrefix.class);
		IUser user = bot.instance.getUserByID(Config.CREATOR_ID);
		return "Who am I you say?" + Config.EOL +
				"I am a person and as for confusing you, that is not my problem :joy:." + Config.EOL +
				"Currently active on **" + bot.instance.getGuilds().size() + "** guilds. " + Config.EOL +
				"There are various actions I can perform actions type **" + cmdPrefix + "help** for a full list" + Config.EOL +
				"If I can't help you out, you can always try to poke **" + user.getName() + "#" + user.getDiscriminator() + "**";
	}
}