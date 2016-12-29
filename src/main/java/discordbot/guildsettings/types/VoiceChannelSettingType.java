package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class VoiceChannelSettingType implements IGuildSettingType {
	@Override
	public String typeName() {
		return "voice-channel";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		if (DisUtil.isChannelMention(value)) {
			return guild.getVoiceChannelById(DisUtil.mentionToId(value)) != null;
		}
		return DisUtil.findVoiceChannel(guild, value) != null;
	}

	@Override
	public String fromInput(Guild guild, String value) {
		if (DisUtil.isChannelMention(value)) {
			VoiceChannel channel = guild.getVoiceChannelById(DisUtil.mentionToId(value));
			if (channel != null) {
				return channel.getId();
			}
		}
		VoiceChannel channel = DisUtil.findVoiceChannel(guild, value);
		if (channel != null) {
			return channel.getId();
		}
		return "";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		VoiceChannel channel = guild.getVoiceChannelById(value);
		if (channel != null) {
			return channel.getName();
		}
		return Emojibet.NO_ENTRY;
	}
}
