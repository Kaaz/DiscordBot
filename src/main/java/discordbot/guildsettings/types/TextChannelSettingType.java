package discordbot.guildsettings.types;

import discordbot.guildsettings.IGuildSettingType;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class TextChannelSettingType implements IGuildSettingType {
	@Override
	public String typeName() {
		return "text-channel";
	}

	@Override
	public boolean validate(Guild guild, String value) {
		if (DisUtil.isChannelMention(value)) {
			return guild.getTextChannelById(DisUtil.mentionToId(value)) != null;
		}
		return DisUtil.findChannel(guild, value) != null;
	}

	@Override
	public String fromInput(Guild guild, String value) {
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
		return Emojibet.NO_ENTRY;
	}
}
