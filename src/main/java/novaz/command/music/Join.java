package novaz.command.music;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * !joinme
 * make the bot join the channel of the user
 */
public class Join extends AbstractCommand {
	public Join(NovaBot b) {
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
	public boolean isAllowedInPrivateChannel() {
		return false;
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length == 0) {
			IVoiceChannel voiceChannel = author.getConnectedVoiceChannels().get(0);
			if (voiceChannel == null) {
				return TextHandler.get("command_join_cantfindyou");
			}
			if (voiceChannel.equals(getCurrentVoiceChannel(channel.getGuild()))) {
				return TextHandler.get("command_join_already_there");
			}
			try {
				leaveCurrentChannel(channel.getGuild());
				voiceChannel.join();
			} catch (MissingPermissionsException e) {
				return TextHandler.get("command_join_nopermssiontojoin");
			}
			return TextHandler.get("command_join_joinedyou");
		} else {
			String channelname = Misc.concat(args);
			IVoiceChannel targetChannel = null;
			for (IVoiceChannel vc : channel.getGuild().getVoiceChannels()) {
				if (vc.getName().equalsIgnoreCase(channelname)) {
					targetChannel = vc;
					break;
				}
			}
			if (targetChannel != null) {
				if (targetChannel.equals(getCurrentVoiceChannel(channel.getGuild()))) {
					return TextHandler.get("command_join_already_there");
				}
				try {
					leaveCurrentChannel(channel.getGuild());
					targetChannel.join();
				} catch (MissingPermissionsException e) {
					return TextHandler.get("command_join_nopermssiontojoin");
				}
				return TextHandler.get("command_join_joined");
			}
			return TextHandler.get("command_join_cantfindchannel");
		}
	}

	private IVoiceChannel getCurrentVoiceChannel(IGuild guild) {
		for (IVoiceChannel channel : bot.instance.getConnectedVoiceChannels()) {
			if (channel.getGuild().equals(guild)) {
				return channel;
			}
		}
		return null;
	}

	private void leaveCurrentChannel(IGuild guild) {
		IVoiceChannel currentVoiceChannel = getCurrentVoiceChannel(guild);
		if (currentVoiceChannel != null) {
			currentVoiceChannel.leave();
		}
	}
}