package discordbot.util;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.db.controllers.CGuild;
import discordbot.db.model.OGuild;
import discordbot.guildsettings.DefaultGuildSettings;
import discordbot.guildsettings.defaults.SettingCommandPrefix;
import discordbot.guildsettings.defaults.SettingUseEconomy;
import discordbot.handler.GuildSettings;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for discord objects
 */
public class DisUtil {
	private static final Pattern mentionUserPattern = Pattern.compile("<@!?([0-9]{4,})>");
	private static final Pattern channelPattern = Pattern.compile("<#!?([0-9]{4,})>");
	private static final Pattern anyMention = Pattern.compile("<[@#]!?([0-9]{4,})>");

	/**
	 * find a text channel by name
	 *
	 * @param guild       the guild to search in
	 * @param channelName the channel to search for
	 * @return TextChannel || null
	 */
	public static TextChannel findChannel(Guild guild, String channelName) {
		for (TextChannel channel : guild.getTextChannels()) {
			if (channel.getName().equalsIgnoreCase(channelName)) {
				return channel;
			}
		}
		return null;
	}

	/**
	 * Gets the first channel in a guild where the bot has permission to write
	 *
	 * @param guild the guild to search in
	 * @return TextChannel || null
	 */
	public static TextChannel findFirstWriteableChannel(JDA client, Guild guild) {
		for (TextChannel channel : guild.getTextChannels()) {
			if (channel.canTalk()) {
				return channel;
			}
		}
		return null;
	}

	/**
	 * Search for a guild
	 *
	 * @param searchArg text to look for
	 *                  with i-prefix searches for internal guild-id
	 *                  if it consists of at least 10 decimals, it will assume its a discord-id
	 * @param container the container to look in
	 * @return Guild || null
	 */
	public static Guild findGuildBy(String searchArg, BotContainer container) {
		if (searchArg.matches("i\\d+")) {
			OGuild rec = CGuild.findById(Integer.parseInt(searchArg.substring(1)));
			if (rec.id > 0) {
				return container.getShardFor(rec.discord_id).client.getGuildById(rec.discord_id);
			}
		} else if (searchArg.matches("^\\d{10,}$")) {
			return container.getShardFor(searchArg).client.getGuildById(searchArg);
		}
		return null;
	}

	/**
	 * Helper for {@link DisUtil#findGuildBy(String, BotContainer)}
	 * validates if the input could be converted to a guild
	 *
	 * @param searchArg text to validate
	 * @return is valid input?
	 */
	public static boolean matchesGuildSearch(String searchArg) {
		return searchArg != null && (searchArg.matches("i\\d+") || searchArg.matches("^\\d{10,}$"));
	}

	/**
	 * Checks if the string contains a mention for a user
	 *
	 * @param input string to check for mentions
	 * @return found a mention
	 */
	public static boolean isUserMention(String input) {
		return mentionUserPattern.matcher(input).find();
	}

	/**
	 * helper method to see if a guild uses the economy module
	 *
	 * @param channel channel to check
	 * @return use economy?
	 */
	public static boolean useEconomy(Channel channel) {
		if (channel != null && channel instanceof TextChannel) {
			return GuildSettings.getFor(((TextChannel) channel), SettingUseEconomy.class).equals("true");
		}
		return false;
	}

	/**
	 * Replaces tags with a variable
	 *
	 * @param input   the message to replace tags in
	 * @param user    user info for user related tags
	 * @param channel channel/guild info
	 * @return formatted string
	 */
	public static String replaceTags(String input, User user, MessageChannel channel) {
		return replaceTags(input, user, channel, null);
	}

	/**
	 * Replaces tags with a variable
	 *
	 * @param input   the message to replace tags in
	 * @param user    user info for user related tags
	 * @param channel channel/guild info
	 * @return formatted string
	 */

	public static String replaceTags(String input, User user, MessageChannel channel, String[] userArgs) {
		Guild guild = null;
		if (channel instanceof TextChannel) {
			guild = ((TextChannel) channel).getGuild();
		}
		String output = input.replace("\\%", "\u0013");
		output = output
				.replace("%user%", user.getName())
				.replace("%user-mention%", user.getAsMention())
				.replace("%user-id%", user.getId())
				.replace("%nick%", guild != null ? guild.isMember(user) ? guild.getMember(user).getEffectiveName() : user.getName() : user.getName())
				.replace("%discrim%", user.getDiscriminator())
				.replace("%guild%", (guild == null) ? "Private" : guild.getName())
				.replace("%guild-id%", (guild == null) ? "0" : guild.getId())
				.replace("%guild-users%", (guild == null) ? "0" : guild.getMembers().size() + "")
				.replace("%channel%", (guild == null) ? "Private" : channel.getName())
				.replace("%channel-id%", (guild == null) ? "0" : channel.getId())
				.replace("%channel-mention%", (guild == null) ? "Private" : ((TextChannel) channel).getAsMention());
		if (guild == null) {
			return output.replace("\u0013", "%");
		}
		if (userArgs != null && output.contains("%arg")) {
			String allArgs = Joiner.on(" ").join(userArgs);
			output = output.replace("%args%", allArgs);
			for (int i = 0; i < userArgs.length; i++) {
				output = output.replace("%arg" + (i + 1) + "%", userArgs[i]);
			}
		}
		int ind;
		Random rng = new Random();
		while ((ind = output.indexOf("%rand-user%")) != -1) {
			output = output.substring(0, ind) +
					guild.getMembers().get(rng.nextInt(guild.getMembers().size())).getEffectiveName()
					+ output.substring(ind + 11);
		}

		if (output.contains("%rand-user-online%")) {
			List<Member> onlines = new ArrayList<>();
			guild.getMembers().stream().filter((u) -> (u.getOnlineStatus().equals(OnlineStatus.ONLINE))).forEach(onlines::add);
			while ((ind = output.indexOf("%rand-user-online%")) != -1)
				output = output.substring(0, ind) +
						onlines.get(rng.nextInt(onlines.size())).getEffectiveName()
						+ output.substring(ind + 18);
		}
		return output.replace("\u0013", "%");
	}

	/**
	 * Attempts to find a user in a channel, first look for account name then for nickname
	 *
	 * @param channel    the channel to look in
	 * @param searchText the name to look for
	 * @return IUser | null
	 */
	public static Member findUserIn(TextChannel channel, String searchText) {
		List<Member> users = channel.getMembers();
		List<Member> potential = new ArrayList<>();
		int smallestDiffIndex = 0, smallestDiff = -1;
		for (Member u : users) {
			String nick = u.getEffectiveName();
			if (nick.equalsIgnoreCase(searchText)) {
				return u;
			}
			if (nick.toLowerCase().contains(searchText)) {
				potential.add(u);
				int d = Math.abs(nick.length() - searchText.length());
				if (d < smallestDiff || smallestDiff == -1) {
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
	public static String filterPrefix(String command, MessageChannel channel) {
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
	public static String getCommandPrefix(MessageChannel channel) {
		if (channel instanceof TextChannel) {
			return getCommandPrefix(((TextChannel) channel).getGuild());
		}
		return DefaultGuildSettings.getDefault(SettingCommandPrefix.class);
	}

	public static String getCommandPrefix(Guild guild) {
		return getCommandPrefix(guild.getId());
	}

	public static String getCommandPrefix(String guildId) {
		if (guildId != null) {
			return GuildSettings.get(guildId).getOrDefault(SettingCommandPrefix.class);
		}
		return Config.BOT_COMMAND_PREFIX;
	}

	/**
	 * Gets a list of users with a certain role within a guild
	 *
	 * @param guild guild to search in
	 * @param role  the role to search for
	 * @return list of user with specified role
	 */
	public static List<Member> getUsersByRole(Guild guild, Role role) {
		return guild.getMembersWithRoles(role);
	}

	/**
	 * Checks if a user has a guild within a guild
	 *
	 * @param user       the user to check
	 * @param guild      the guild to check in
	 * @param permission the permission to check for
	 * @return permission found
	 */
	public static boolean hasPermission(User user, Guild guild, Permission permission) {
		return PermissionUtil.checkPermission(guild, guild.getMember(user), permission);
	}

	/**
	 * attempts to find a role within a guild
	 *
	 * @param guild    the guild to search in
	 * @param roleName the role name to search for
	 * @return role or null
	 */
	public static Role findRole(Guild guild, String roleName) {
		List<Role> roles = guild.getRoles();
		for (Role role : roles) {
			if (role.getName().equalsIgnoreCase(roleName)) {
				return role;
			}
		}
		return null;
	}
}
