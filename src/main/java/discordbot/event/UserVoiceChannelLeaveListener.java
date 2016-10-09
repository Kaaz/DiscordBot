package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * If a user leaves a voice channel
 */
public class UserVoiceChannelLeaveListener extends AbstractEventListener<UserVoiceChannelLeaveEvent> {
	public UserVoiceChannelLeaveListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(UserVoiceChannelLeaveEvent event) {
		IVoiceChannel channel = event.getChannel();
		IVoiceChannel connectedVoice = null;
		for (IVoiceChannel voiceChannel : discordBot.client.getConnectedVoiceChannels()) {
			if (voiceChannel.getID().equals(channel.getID())) {
				connectedVoice = voiceChannel;
				break;
			}
		}
		if (connectedVoice != null) {
			boolean shouldLeave = true;
			for (IUser user : connectedVoice.getConnectedUsers()) {
				if (!user.isBot()) {
					shouldLeave = false;
					break;
				}
			}
			if (shouldLeave) {
				MusicPlayerHandler.getFor(channel.getGuild(), discordBot).stopMusic();
				connectedVoice.leave();
				discordBot.out.sendMessage(discordBot.getMusicChannel(channel.getGuild()), Template.get("music_no_one_listens_i_leave"));
			}
		}
	}

}