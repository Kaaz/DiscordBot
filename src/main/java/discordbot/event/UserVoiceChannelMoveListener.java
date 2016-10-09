package discordbot.event;

import discordbot.core.AbstractEventListener;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.impl.events.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * If a user leaves a voice channel
 */
public class UserVoiceChannelMoveListener extends AbstractEventListener<UserVoiceChannelMoveEvent> {
	public UserVoiceChannelMoveListener(DiscordBot discordBot) {
		super(discordBot);
	}

	@Override
	public boolean listenerIsActivated() {
		return true;
	}

	@Override
	public void handle(UserVoiceChannelMoveEvent event) {
		IVoiceChannel connectedVoice = null;
		IGuild guild = event.getNewChannel().getGuild();
		for (IVoiceChannel voiceChannel : discordBot.client.getConnectedVoiceChannels()) {
			if (voiceChannel.getGuild().getID().equals(guild.getID())) {
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
				MusicPlayerHandler.getFor(guild, discordBot).stopMusic();
				connectedVoice.leave();
				discordBot.out.sendMessage(discordBot.getMusicChannel(guild), Template.get("music_no_one_listens_i_leave"));
			}
		}
	}

}