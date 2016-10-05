package discordbot.util;

import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.handler.GuildSettings;
import sx.blah.discord.handle.obj.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
	 * Attempts to find a user in a channel, first look for account name then for nickname
	 *
	 * @param channel    the channel to look in
	 * @param searchText the name to look for
	 * @return IUser | null
	 */
	public static IUser findUserIn(IChannel channel, String searchText) {
		List<IUser> users = channel.getUsersHere();
		List<IUser> potential = new ArrayList<>();
		int smallestDiffIndex = 0, smallestDiff = 999;
		for (IUser u : users) {
			if (u.getName().equalsIgnoreCase(searchText)) {
				return u;
			}
			Optional<String> nickNameOptional = u.getNicknameForGuild(channel.getGuild());
			String nick;
			if (nickNameOptional.isPresent()) {
				nick = nickNameOptional.get().toLowerCase();
			} else {
				nick = u.getName().toLowerCase();
			}
			if (nick.contains(searchText)) {
				potential.add(u);
				int d = Math.abs(nick.length() - searchText.length());
				if (d < smallestDiff) {
					smallestDiff = d;
					smallestDiffIndex = potential.size() - 1;
				}
			}

		}
		if (!potential.isEmpty()) {
			return potential.get(smallestDiffIndex);
		}
		return null;
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

	/**
	 * Checks if a user has a guild within a guild
	 *
	 * @param user       the user to check
	 * @param guild      the guild to check in
	 * @param permission the permission to check for
	 * @return permission found
	 */
	public static boolean hasPermission(IUser user, IGuild guild, Permissions permission) {
		if (guild == null) {
			return false;
		}
		List<IRole> roles = guild.getRolesForUser(user);
		for (IRole role : roles) {
			if (role.getPermissions().contains(permission)) {
				return true;
			}
		}
		return false;
	}
}
