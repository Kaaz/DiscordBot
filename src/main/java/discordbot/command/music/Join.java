package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;

/**
 * !joinme
 * make the bot join the channel of the user
 */
public class Join extends AbstractCommand {
	public Join(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "joins a voicechannel";
	}

	@Override
	public String getCommand() {
		return "join";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"join                //attempts to join you",
				"join <channelname>  //attempts to join channelname"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, MessageChannel channel, User author) {
		TextChannel chan = (TextChannel) channel;
		if (args.length == 0) {
			VoiceChannel voiceChannel = chan.getGuild().getVoiceStatusOfUser(author).getChannel();
			if (voiceChannel == null) {
				return Template.get("command_join_cantfindyou");
			}
			if (bot.isConnectedTo(voiceChannel)) {
				return Template.get("command_join_already_there");
			}
			bot.connectTo(voiceChannel);
			return Template.get("command_join_joinedyou");
		} else {
			String channelname = Misc.concat(args);
			VoiceChannel targetChannel = null;
			for (VoiceChannel vc : chan.getGuild().getVoiceChannels()) {
				if (vc.getName().equalsIgnoreCase(channelname)) {
					targetChannel = vc;
					break;
				}
			}
			if (targetChannel != null) {
				if (bot.isConnectedTo(targetChannel)) {
					return Template.get("command_join_already_there");
				}
				bot.leaveVoice(chan.getGuild());
				bot.connectTo(targetChannel);
//					return Template.get("command_join_nopermssiontojoin");
				return Template.get("command_join_joined");
			}
			return Template.get("command_join_cantfindchannel");
		}
	}
}