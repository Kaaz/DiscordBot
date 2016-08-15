package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * !joinme
 * make the bot join the channel of the user
 */
public class JoinMe extends AbstractCommand {
	public JoinMe(NovaBot b) {
		super(b);
		setCmd("joinme");
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		IVoiceChannel voiceChannel = author.getConnectedVoiceChannels().get(0);
		if (voiceChannel == null) {
			return TextHandler.get("command_joinme_cantfindyou");
		}
		IAudioManager audioManager = channel.getGuild().getAudioManager();
		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e) {
			return TextHandler.get("command_joinme_nopermssiontojoin");
		}
		return TextHandler.get("command_joinme_joinedyou");
	}
}