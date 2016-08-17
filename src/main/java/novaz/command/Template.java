package novaz.command;

import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
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
				"template list //lists all keyphrases",
				"template list <keyphrase> //lists all options for keyphrase",
				"template remove <keyphrase> <index> //removes selected template for keyphrase",
				"template add <keyphrase> <text...> //adds a template for keyphrase",
				"template toggledebug //shows keyphrases instead of text"};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.isOwner(channel.getGuild(), author)) {
			if (args.length == 0) {
				return getUsage()[0];//@todo plz
			}
			if (args.length >= 1) {
				switch (args[0]) {
					case "toggledebug":
						if (bot.isCreator(author)) {
							return "hello " + author.mention();
						}
						return TextHandler.get("not_yet_implemented");
					case "list":
					case "remove":
					case "add":
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