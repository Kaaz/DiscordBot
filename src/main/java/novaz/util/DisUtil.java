package novaz.util;

import novaz.guildsettings.DefaultGuildSettings;
import novaz.guildsettings.defaults.SettingCommandPrefix;
import novaz.handler.GuildSettings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utilities for discord objects
 */
public class DisUtil {
	private static final Pattern mentionUserPattern = Pattern.compile("<@!?([0-9]{4,})>");
	private static final Pattern channelPattern = Pattern.compile("<#!?([0-9]{4,})>");
	private static final Pattern anyMention = Pattern.compile("<[@#]!?([0-9]{4,})>");

	/**
	 * Checks if the string contains a mention for a user
	 *
	 * @param input string to check for mentions
	 * @return found a mention
	 */
	public static boolean isUserMention(String input) {
		return mentionUserPattern.matcher(input).matches();
	}

	/**
	 * @param input string to check for mentions
	 * @return found a mention
	 */
	public static boolean isChannelMention(String input) {
		return channelPattern.matcher(input).matches();
	}

	/**
	 * Converts any mention to an id
	 *
	 * @param mention the mention to filter
	 * @return a stripped down version of the mention
	 */
	public static String mentionToId(String mention) {
		String id = "";
		Matcher matcher = anyMention.matcher(mention);
		if (matcher.find()) {
			id = matcher.group(1);
		}
		return id;
	}

	/**
	 * Retrieve all mentions from an input
	 *
	 * @param input text to check for mentions
	 * @return list of all found mentions
	 */
	public static List<String> getAllMentions(String input) {
		List<String> list = new ArrayList<>();
		Matcher matcher = anyMention.matcher(input);
		while (matcher.find()) {
			list.add(matcher.group(1));
		}
		return list;
	}

	/**
	 * Filters the command prefix from the string
	 *
	 * @param command the text to filter form
	 * @param channel the channel where the text came from
	 * @return text with the prefix filtered
	 */
	public static String filterPrefix(String command, IChannel channel) {
		String prefix = getCommandPrefix(channel);
		if (command.startsWith(prefix)) {
			return command.substring(prefix.length());
		}
		return command;
	}

	/**
	 * gets the command prefix for specified channel
	 *
	 * @param channel channel to check the prefix for
	 * @return the command prefix
	 */
	public static String getCommandPrefix(IChannel channel) {
		if (channel == null || channel.isPrivate()) {
			return DefaultGuildSettings.getDefault(SettingCommandPrefix.class);
		}
		return GuildSettings.get(channel.getGuild()).getOrDefault(SettingCommandPrefix.class);
	}

	/**
	 * Gets a list of users with a certain role within a guild
	 *
	 * @param guild guild to search in
	 * @param role  the role to search for
	 * @return list of user with specified role
	 */
	public static List<IUser> getUsersByRole(IGuild guild, IRole role) {
		return guild.getUsers().stream().filter((users) -> users.getRolesForGuild(guild).contains(role)).collect(Collectors.toList());
	}
}
