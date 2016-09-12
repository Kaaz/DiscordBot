package novaz.command.administrative;

import com.vdurmont.emoji.EmojiParser;
import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

/**
 * !template
 * manages the templates
 */
public class Template extends AbstractCommand {
	public Template(NovaBot b) {
		super(b);
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
				"template list                        //lists all keyphrases",
				"template list <keyphrase>            //lists all options for keyphrase",
				"template remove <keyphrase> <index>  //removes selected template for keyphrase",
				"template add <keyphrase> <text...>   //adds a template for keyphrase",
				"template toggledebug                 //shows keyphrases instead of text"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.isOwner(channel, author)) {
			if (args.length == 0) {
				String usage = ":gear: **Options**:```php" + Config.EOL;
				for (String line : getUsage()) {
					usage += line + Config.EOL;
				}
				return usage + "```";
			}
			if (args.length >= 1) {
				switch (args[0]) {
					case "toggledebug":
						if (bot.isCreator(author)) {
							Config.SHOW_KEYPHRASE = !Config.SHOW_KEYPHRASE;
							if (Config.SHOW_KEYPHRASE) {
								return "Keyphrases shown ";
							} else {
								return "Keyphrases are being translated";
							}
						}
						return TextHandler.get("not_yet_implemented");
					case "add":
						if (args.length >= 3) {
							String text = "";
							for (int i = 2; i < args.length; i++) {
								text += " " + args[i];
							}
							TextHandler.getInstance().add(args[1], EmojiParser.parseToAliases(text));
							return TextHandler.get("command_template_added");
						}
						return TextHandler.get("command_template_added_failed");
					case "remove":
					case "list":
						return TextHandler.get("not_yet_implemented");
					default:
						return TextHandler.get("command_template_invalid_option");
				}
			}
			return TextHandler.get("not_yet_implemented");
		}
		return TextHandler.get("command_template_no_permission");
	}
}