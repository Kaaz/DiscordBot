package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

/**
 * !joinme
 * make the bot join the channel of the user
 */
public class JoinCommand extends AbstractCommand {
	public JoinCommand() {
		super();
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		TextChannel chan = (TextChannel) channel;
		MusicPlayerHandler player = MusicPlayerHandler.getFor(chan.getGuild(), bot);
		if (args.length == 0) {
			VoiceChannel voiceChannel = chan.getGuild().getMember(author).getVoiceState().getChannel();
			if (voiceChannel == null) {
				return Template.get("command_join_cantfindyou");
			}
			if (player.isConnectedTo(voiceChannel)) {
				return Template.get("command_join_already_there");
			}
			if (!PermissionUtil.checkPermission(voiceChannel, voiceChannel.getGuild().getSelfMember(), Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
				return Template.get("music_join_no_permission");
			}
			player.connectTo(voiceChannel);
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
				if (player.isConnectedTo(targetChannel)) {
					return Template.get("command_join_already_there");
				}
				player.leave();
				player.connectTo(targetChannel);
//					return Template.get("command_join_nopermssiontojoin");
				return Template.get("command_join_joined");
			}
			return Template.get("command_join_cantfindchannel");
		}
	}
}