package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !leave
 * make the bot leave
 */
public class Leave extends AbstractCommand {
	public Leave() {
		super();
	}

	@Override
	public String getDescription() {
		return "Leaves the voicechannel";
	}

	@Override
	public String getCommand() {
		return "leave";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (bot.leaveVoice(((TextChannel) channel).getGuild())) {
			return Template.get("command_leave_success");
		}
		return Template.get("command_leave_failed");
	}
}