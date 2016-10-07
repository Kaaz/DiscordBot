package discordbot.command.administrative;

import discordbot.core.AbstractCommand;
import discordbot.db.model.OReplyPattern;
import discordbot.db.table.TReplyPattern;
import discordbot.db.table.TUser;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * managing text replies for the bot
 */
public class TextReplyCommand extends AbstractCommand {
	public final static int MIN_TAG_LENGTH = 2;

	public TextReplyCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Patterns where the bot auto-replies to. ";
	}

	@Override
	public String getCommand() {
		return "textreply";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"tr <create> <tagname>      //creates tag",
				"tr regex <tag> <value>     //edit the regex of a tag",
				"tr response <tag> <value>  //change the response of a reply",
				"tr tag <tag> <value>       //change the tag of a reply",
				"tr cd <tag> <value>        //change the cooldown (millis) of a reply",
				"tr guild <tag> <guildid>   //guild of a tag, 0 for global",
				"tr test <tag> <text>       //test for a match",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"tr"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isCreator(author)) {
			return Template.get("no_permission");
		}
		if (args.length == 0) {
			return Template.get("command_invalid_use");
		}
		if (args.length >= 2) {
			if (args[1].length() < MIN_TAG_LENGTH) {
				return Template.get("command_textreply_tag_length", MIN_TAG_LENGTH);
			}
			OReplyPattern replyPattern = TReplyPattern.findBy(args[1]);
			if (args[0].equals("create")) {
				if (replyPattern.id == 0) {
					replyPattern.tag = args[1];
					replyPattern.userId = TUser.getCachedId(author.getID());
					TReplyPattern.insert(replyPattern);
					return Template.get("command_textreply_created", args[1]);
				}
				return Template.get("command_textreply_already_exists", args[1]);
			}
			if (replyPattern.id == 0) {
				return Template.get("command_textreply_not_exists", args[1]);
			}
			String restOfArgs = "";
			for (int i = 2; i < args.length; i++) {
				restOfArgs += args[i];
				if (i != args.length - 1) {
					restOfArgs += " ";
				}
			}
			switch (args[0].toLowerCase()) {
				case "regex":
				case "pattern":
					try {
						Pattern pattern = Pattern.compile(restOfArgs);
						replyPattern.pattern = restOfArgs;
						TReplyPattern.update(replyPattern);
						System.out.println(restOfArgs);
						System.out.println(pattern.pattern());
					} catch (PatternSyntaxException exception) {
						return Template.get("command_textreply_regex_invalid") + Config.EOL +
								exception.getDescription() + Config.EOL +
								Misc.makeTable(exception.getMessage());
					}
					return Template.get("command_textreply_regex_saved");//"Your regex is :+1:";
				case "response":
				case "reply":
					replyPattern.reply = restOfArgs;
					TReplyPattern.update(replyPattern);
					return Template.get("command_textreply_response_saved");
				case "tag":
					replyPattern.tag = args[2];
					TReplyPattern.update(replyPattern);
					return Template.get("command_textreply_tag_saved");
				case "cd":
				case "cooldown":
					replyPattern.cooldown = Long.parseLong(args[2]);
					TReplyPattern.update(replyPattern);
					return Template.get("command_textreply_cooldown_saved");
				case "test":
					Pattern pattern = Pattern.compile(replyPattern.pattern);
					Matcher matcher = pattern.matcher(restOfArgs);
					if (matcher.find()) {
//						return String.format("`%s` matches `%s`", restOfArgs, replyPattern.pattern);
						return replyPattern.reply;
					}
					return Template.get("command_textreply_no_match");
				default:
					return Template.get("invalid_use");
			}
		}
		if (args.length == 1) {
			OReplyPattern replyPattern = TReplyPattern.findBy(args[0]);
			if (replyPattern.id == 0) {
				return Template.get("command_textreply_not_exists", args[0]);
			}
			List<List<String>> tbl = new ArrayList<>();
			tbl.add(Arrays.asList("created on ", "" + replyPattern.createdOn));
			tbl.add(Arrays.asList("tag", replyPattern.tag));
			tbl.add(Arrays.asList("creator", "" + replyPattern.userId));
			tbl.add(Arrays.asList("guild", "" + replyPattern.guildId));
			tbl.add(Arrays.asList("pattern", "" + replyPattern.pattern));
			tbl.add(Arrays.asList("reply", "" + replyPattern.reply));
			tbl.add(Arrays.asList("cooldown (milli)", "" + replyPattern.cooldown));
			System.out.println(replyPattern.pattern);

			return Misc.makeAsciiTable(Arrays.asList("Property", "Value"), tbl);
		}
		return Template.get("invalid_use");
	}
}