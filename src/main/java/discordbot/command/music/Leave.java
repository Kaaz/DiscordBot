package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.List;

/**
 * !leave
 * make the bot leave
 */
public class Leave extends AbstractCommand {
	public Leave(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, IChannel channel, IUser author) {
		boolean leftSomething = false;
		List<IVoiceChannel> connectedVoiceChannels = bot.instance.getConnectedVoiceChannels();
		for (IVoiceChannel voicechan : connectedVoiceChannels) {
			if (voicechan.getGuild().equals(channel.getGuild())) {
				voicechan.leave();
				bot.stopMusic(channel.getGuild());
				leftSomething = true;
			}
		}
		if (leftSomething) {
			return Template.get("command_leave_success");
		}
		return Template.get("command_leave_failed");
	}
}