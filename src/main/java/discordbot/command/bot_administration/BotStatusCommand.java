package discordbot.command.bot_administration;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Emojibet;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

/**
 * !botstatus
 * changes the bot status (the playing game, or streaming)
 */
public class BotStatusCommand extends AbstractCommand {
	public BotStatusCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Set the game I'm currently playing";
	}

	@Override
	public String getCommand() {
		return "botstatus";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"botstatus reset                      //unlocks the status",
				"botstatus game <game>                //changes the playing game to <game>",
				"botstatus stream <username> <game>   //streaming twitch.tv/<username> playing <game>",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank rank = bot.security.getSimpleRank(author);
		if (!rank.isAtLeast(SimpleRank.BOT_ADMIN)) {
			return Template.get(channel, "command_no_permission");
		}
		if (args.length == 0) {
			return Template.get("command_invalid_use");
		}
		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
				case "reset":
					bot.getContainer().setStatusLocked(false);
					return Emojibet.THUMBS_UP;
				case "game":
					if (args.length < 2) {
						return Template.get("command_invalid_use");
					}
					bot.client.getAccountManager().setGame(Misc.joinStrings(args, 1));
					break;
				case "stream":
					if (args.length < 3) {
						return Template.get("command_invalid_use");
					}
					try {
						bot.client.getAccountManager().setStreaming(Misc.joinStrings(args, 2), "http://www.twitch.tv/" + args[1]);
					} catch (Exception e) {
						return Emojibet.THUMBS_DOWN + " " + e.getMessage();
					}
					break;
				default:
					return Template.get("command_invalid_use");
			}
			bot.getContainer().setStatusLocked(true);
			try {
				Thread.sleep(5_000L);
			} catch (InterruptedException ignored) {
			}
			bot.client.getAccountManager().update();
			return Emojibet.THUMBS_UP;
		}
		return Template.get("command_invalid_use");
	}
}