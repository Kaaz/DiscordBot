package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * TextChannel settings type
 * the value has to be a real channel in a guild + will be saved as the channel id
 */
public class TextChannelSettingType implements IGuildSettingType {

	private final boolean allowNull;

	/**
	 * Allow a null/false value?
	 *
	 * @param allowNull true if it can be null
	 */
	public TextChannelSettingType(boolean allowNull) {

		this.allowNull = allowNull;
	}

	@Override
	public String typeName() {
		return "text-channel";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		if (allowNull && (value == null || value.isEmpty() || value.equalsIgnoreCase("false"))) {
			return true;
		}
		if (DisUtil.isChannelMention(value)) {
			return guild.getTextChannelById(DisUtil.mentionToId(value)) != null;
		}
		return DisUtil.findChannel(guild, value) != null;
	}

	@Override
	public String fromInput(Guild guild, String value) {
		if (allowNull && (value == null || value.isEmpty() || value.equalsIgnoreCase("false"))) {
			return "";
		}
		if (DisUtil.isChannelMention(value)) {
			TextChannel textChannel = guild.getTextChannelById(DisUtil.mentionToId(value));
			if (textChannel != null) {
				return textChannel.getId();
			}
		}
		TextChannel channel = DisUtil.findChannel(guild, value);
		if (channel != null) {
			return channel.getId();
		}
		return "";
	}

	@Override
	public String toDisplay(Guild guild, String value) {
		TextChannel channel = guild.getTextChannelById(value);
		if (channel != null) {
			return channel.getAsMention();
		}
		if (!value.isEmpty() && !value.matches("\\d{10,}")) {
			TextChannel channelByName = DisUtil.findChannel(guild, value);
			if (channelByName != null) {
				return channelByName.getAsMention();
			}
		}
		return Emojibet.NO_ENTRY;
	}
}
