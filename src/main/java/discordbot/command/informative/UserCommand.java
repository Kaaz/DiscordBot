package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.db.model.OGuildMember;
import discordbot.db.model.OUser;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CGuildMember;
import discordbot.db.controllers.CUser;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import discordbot.util.TimeUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !user
 * shows some info about the user
 */
public class UserCommand extends AbstractCommand {
	public UserCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Shows information about the user";
	}

	@Override
	public String getCommand() {
		return "user";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"user         //info about you",
				"user @user   //info about @user"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"whois"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		User infoUser = null;
		if (args.length == 0) {
			infoUser = author;
		} else if (DisUtil.isUserMention(args[0])) {
			infoUser = bot.client.getUserById(DisUtil.mentionToId(args[0]));
		} else if (args[0].matches("i\\d+")) {
			OUser dbUser = CUser.findById(Integer.parseInt(args[0].substring(1)));
			infoUser = bot.client.getUserById(dbUser.discord_id);
		} else if (channel instanceof TextChannel) {
			infoUser = DisUtil.findUserIn((TextChannel) channel, args[0]);
		}
		if (infoUser != null) {
			int userId = CUser.getCachedId(infoUser.getId());
			int guildId = 0;
			StringBuilder sb = new StringBuilder();
			String nickname = infoUser.getUsername();
			if (channel instanceof TextChannel) {
				guildId = CGuild.getCachedId(((TextChannel) channel).getGuild().getId());
				nickname = ((TextChannel) channel).getGuild().getNicknameForUser(infoUser);
				if (nickname == null) {
					nickname = infoUser.getUsername();
				}
			}
			sb.append("Querying for ").append(nickname).append(Config.EOL);
			sb.append(":bust_in_silhouette: User: ").append(infoUser.getUsername()).append("#").append(infoUser.getDiscriminator()).append(Config.EOL);
//			sb.append(":date: Account registered at ").append(infoUser.()).append(Config.EOL);
			sb.append(":id: discord id:").append(infoUser.getId()).append(Config.EOL);
			if (guildId > 0) {
				OGuildMember member = CGuildMember.findBy(guildId, userId);
				if (member.joinDate != null) {
					sb.append(":date: joined: ").append(TimeUtil.getRelativeTime(member.joinDate.getTime() / 1000L, false, true)).append(Config.EOL);
				}
			}
			if (!infoUser.getAvatarUrl().endsWith("null.jpg")) {
				sb.append(":frame_photo: Avatar: ").append("`").append(infoUser.getAvatarUrl()).append("`").append(Config.EOL);
			}
			if (infoUser.isBot()) {
				sb.append(":robot: This user is a bot (or pretends to be)");
			}
			return sb.toString();
		}


		return Template.get("command_user_not_found");
	}
}