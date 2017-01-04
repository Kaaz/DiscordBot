package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * VoiceChannel settings type
 * the value has to be a real voice-channel in a guild + will be saved as the channel id
 */
public class VoiceChannelSettingType implements IGuildSettingType {
	private final boolean allowNull;

	public VoiceChannelSettingType(boolean allowNull) {

		this.allowNull = allowNull;
	}

	@Override
	public String typeName() {
		return "voice-channel";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		if (allowNull && (value == null || value.isEmpty() || value.equalsIgnoreCase("false"))) {
			return true;
		}
		if (DisUtil.isChannelMention(value)) {
			return guild.getVoiceChannelById(DisUtil.mentionToId(value)) != null;
		}
		return DisUtil.findVoiceChannel(guild, value) != null;
	}

	@Override
	public String fromInput(Guild guild, String value) {
		if (allowNull && (value == null || value.isEmpty() || value.equalsIgnoreCase("false"))) {
			return "false";
		}
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
		return "false";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		VoiceChannel channel = guild.getVoiceChannelById(value);
		if (channel != null) {
			return channel.getName();
		}
		if (!value.isEmpty() && !value.matches("\\d{10,}")) {
			VoiceChannel channelByName = DisUtil.findVoiceChannel(guild, value);
			if (channelByName != null) {
				return channelByName.getName();
			}
		}
		return Emojibet.NO_ENTRY;
	}
}
