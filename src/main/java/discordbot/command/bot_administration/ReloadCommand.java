package discordbot.command.bot_administration;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !reload
 * reloads config
 */
public class ReloadCommand extends AbstractCommand {
	public ReloadCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "reloads the configuration";
	}

	@Override
	public String getCommand() {
		return "reload";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
			bot.loadConfiguration();
			return Template.get("command_reload_success");
		}
		if (rank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			bot.clearChannels(guild);
			bot.loadGuild(guild);
			return Template.get("command_reload_success");
		}
		return Template.get("no_permission");
	}
}