package discordbot.command.bot_administration;

import com.vdurmont.emoji.EmojiParser;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Misc;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * !template
 * manages the templates
 */
public class TemplateCommand extends AbstractCommand {
	public TemplateCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "adds/removes templates";
	}

	@Override
	public String getCommand() {
		return "template";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"template list <page>                 //lists all keyphrases",
				"template list <contains>     		  //lists all options for keyphrase",
				"template remove <keyphrase> <index>  //removes selected template for keyphrase",
				"template add <keyphrase> <text...>   //adds a template for keyphrase"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"tpl"
		};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		SimpleRank userRank = bot.security.getSimpleRank(author);
		int guildId = 0;
		if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
			return Template.get(channel, "no_permission");
		}
		if (!userRank.isAtLeast(SimpleRank.BOT_ADMIN)) {
			if (!(channel instanceof TextChannel)) {
				return Template.get(channel, "command_not_for_private ");
			}
			guildId = CGuild.getCachedId(channel);
		}
		if (args.length == 0) {
			String usage = ":gear: **Options**:```php" + Config.EOL;
			for (String line : getUsage()) {
				usage += line + Config.EOL;
			}
			return usage + "```";
		}
		switch (args[0]) {
			case "toggledebug":
				if (userRank.isAtLeast(SimpleRank.BOT_ADMIN)) {
					Config.SHOW_KEYPHRASE = !Config.SHOW_KEYPHRASE;
					if (Config.SHOW_KEYPHRASE) {
						return "Keyphrases shown ";
					} else {
						return "Keyphrases are being translated";
					}
				}
				return Template.get(channel, "no_permission");
			case "add":
				if (args.length >= 3) {
					String text = args[2];
					for (int i = 3; i < args.length; i++) {
						text += " " + args[i];
					}
					Template.add(guildId, args[1], EmojiParser.parseToAliases(text));
					return Template.get(channel, "command_template_added");
				}
				return Template.get(channel, "command_template_added_failed");
			case "delete":
			case "del":
			case "remove":
				if (args.length < 3 || !args[2].matches("^\\d+$")) {
					return Template.get(channel, "command_template_invalid_option");
				}
				int deleteIndex = Integer.parseInt(args[2]);
				List<String> templateList = Template.getAllFor(guildId, args[1]);
				if (templateList.size() > deleteIndex) {
					Template.remove(guildId, args[1], templateList.get(deleteIndex));
					return Template.get(channel, "command_template_delete_success");
				}
				return Template.get(channel, "command_template_delete_failed");
			case "list":
				int currentPage = 0;
				int itemsPerPage = 30;
				int maxPage = 1 + Template.uniquePhraseCount() / itemsPerPage;
				if (args.length >= 2) {
					if (args[1].matches("^\\d+$")) {
						currentPage = Math.min(Math.max(0, Integer.parseInt(args[1]) - 1), maxPage - 1);
					} else {
						List<String> allKeyphrases = Template.getAllKeyphrases(args[1], itemsPerPage, 0);
						if (allKeyphrases.isEmpty()) {
							return "No keyphases matching `" + args[1] + "`";
						}
						return String.format("All keyphrases matching `%s`: ", args[1]) + Config.EOL +
								Misc.makeTable(allKeyphrases, 50, 2);
					}
				}
				List<String> allKeyphrases = Template.getAllKeyphrases(itemsPerPage, currentPage * itemsPerPage);
				if (allKeyphrases.isEmpty()) {
					return "No keyphrases set at this moment.";
				}
				return String.format("All keyphrases: [page %s/%s]", currentPage + 1, maxPage) + Config.EOL +
						Misc.makeTable(allKeyphrases, 50, 2);
			default:
				args[0] = args[0].toLowerCase();
				List<String> templates = Template.getAllFor(guildId, args[0]);
				if (args.length == 1) {
					if (templates.isEmpty()) {
						return Template.get(channel, "command_template_not_found", args[0]);
					}
					List<List<String>> body = new ArrayList<>();
					int index = 0;
					for (String template : templates) {
						body.add(Arrays.asList(String.valueOf(index++), template));
					}
					return "Template overview for `" + args[0] + "`" + Config.EOL +
							Misc.makeAsciiTable(Arrays.asList("#", "value"), body, null);
				}
				return Template.get(channel, "command_template_invalid_option");
		}
	}
}