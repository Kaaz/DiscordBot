package discordbot.command.informative;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class PrefixCommand extends AbstractCommand {
	public PrefixCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Forgot what the prefix is? I got you covered";
	}

	@Override
	public String getCommand() {
		return "prefix";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"prefix                           //shows the set prefix",
				"prefix <prefix>                  //sets the prefix to <prefix>",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (args.length > 0 && rank.isAtLeast(SimpleRank.GUILD_ADMIN) && channel instanceof TextChannel) {
			TextChannel text = (TextChannel) channel;
			GuildSettings guildSettings = GuildSettings.get(text.getGuild());
			if (guildSettings.set(SettingCommandPrefix.class, args[0])) {
				return Template.get(channel, "command_prefix_saved", args[0]);
			}
			return Template.get(channel, "command_prefix_invalid",
					args[0],
					"```" + Config.EOL + Joiner.on(Config.EOL).join(guildSettings.getDescription(SettingCommandPrefix.class)) + Config.EOL + "```");
		}
		return Template.get(channel, "command_prefix_is", DisUtil.getCommandPrefix(channel));
	}
}