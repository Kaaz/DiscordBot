package discordbot.command.informative;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.DisUtil;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !user
 * shows some info about the user
 */
public class UserCommand extends AbstractCommand {
	public UserCommand(DiscordBot b) {
		super(b);
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
	public String execute(String[] args, MessageChannel channel, User author) {
		User infoUser = null;
		if (args.length == 0) {
			infoUser = author;
		} else if (DisUtil.isUserMention(args[0])) {
			infoUser = bot.client.getUserById(DisUtil.mentionToId(args[0]));
		}

		if (infoUser != null) {
			StringBuilder sb = new StringBuilder();
			String nickname = infoUser.getUsername();
			if (!(channel instanceof PrivateChannel)) {
				nickname = ((TextChannel) channel).getGuild().getNicknameForUser(infoUser);
				if (nickname == null) {
					nickname = infoUser.getUsername();
				}
			}
			sb.append("Querying for ").append(nickname).append(Config.EOL);
			sb.append(":bust_in_silhouette: User: ").append(infoUser.getUsername()).append("#").append(infoUser.getDiscriminator()).append(Config.EOL);
//			sb.append(":date: Account registered at ").append(infoUser.()).append(Config.EOL);
			sb.append(":id: : ").append(infoUser.getId()).append(Config.EOL);
			System.out.println(infoUser.getAvatarUrl());
			if (!infoUser.getAvatarUrl().endsWith("null.jpg")) {
				sb.append(":frame_photo: Avatar: ").append(infoUser.getAvatarUrl());
			}
			if (infoUser.isBot()) {
				sb.append(Config.EOL).append(":robot: This user is a bot (or pretends to be)");
			}
			return sb.toString();
		}


		return Template.get("command_user_not_found");
	}
}