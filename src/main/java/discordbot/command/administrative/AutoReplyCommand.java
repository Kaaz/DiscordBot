package discordbot.command.administrative;

import com.vdurmont.emoji.EmojiParser;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OReplyPattern;
import discordbot.db.model.OServer;
import discordbot.db.table.TReplyPattern;
import discordbot.db.table.TServers;
import discordbot.db.table.TUser;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import discordbot.util.TimeUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * managing auto replies for the bot
 */
public class AutoReplyCommand extends AbstractCommand {
	public final static int MIN_TAG_LENGTH = 2;

	public AutoReplyCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Patterns where the bot auto-replies to. ";
	}

	@Override
	public String getCommand() {
		return "autoreply";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"ar <create> <tagname>      //creates tag",
				"ar regex <tag> <value>     //edit the regex of a tag",
				"ar response <tag> <value>  //change the response of a reply",
				"ar tag <tag> <value>       //change the tag of a reply",
				"ar cd <tag> <value>        //change the cooldown (millis) of a reply",
				"ar guild <tag> <guildid>   //guild of a tag, 0 for global",
				"ar test <tag> <text>       //test for a match",
				"ar delete <tag>            //deletes a tag",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"ar"
		};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (!bot.isAdmin(channel, author)) {
			return Template.get("no_permission");
		}
		if (args.length == 0) {

			List<OReplyPattern> all = TReplyPattern.getAll();
			List<List<String>> tbl = new ArrayList<>();
			for (OReplyPattern replyPattern : all) {
				List<String> row = new ArrayList<>();

				row.add(replyPattern.tag);
				row.add(replyPattern.pattern);
				row.add(TimeUtil.getRelativeTime((System.currentTimeMillis() + replyPattern.cooldown + 1000L) / 1000L, false, false));
				row.add(replyPattern.reply.substring(0, Math.min(40, replyPattern.reply.length())));
				tbl.add(row);
			}
			return "The following All Auto replies information. For details about a specific one use **`ar <tag>`**" + Config.EOL +
					Misc.makeAsciiTable(Arrays.asList("tag", "trigger", "cooldown", "response"), tbl);
		}
		if (args.length >= 2) {
			if (args[1].length() < MIN_TAG_LENGTH) {
				return Template.get("command_autoreply_tag_length", MIN_TAG_LENGTH);
			}
			OReplyPattern replyPattern = TReplyPattern.findBy(args[1]);
			if (args[0].equals("create")) {
				if (replyPattern.id == 0) {
					replyPattern.tag = args[1];
					replyPattern.userId = TUser.getCachedId(author.getID());
					replyPattern.guildId = bot.isCreator(author) ? 0 : TServers.getCachedId(channel.getGuild().getID());
					TReplyPattern.insert(replyPattern);
					return Template.get("command_autoreply_created", args[1]);
				}
				return Template.get("command_autoreply_already_exists", args[1]);
			}
			if (replyPattern.id == 0) {
				return Template.get("command_autoreply_not_exists", args[1]);
			}
			String restOfArgs = "";
			for (int i = 2; i < args.length; i++) {
				restOfArgs += args[i];
				if (i != args.length - 1) {
					restOfArgs += " ";
				}
			}
			switch (args[0].toLowerCase()) {
				case "delete":
				case "remove":
				case "del":
					if (bot.isCreator(author) || (bot.isAdmin(channel, author) && TServers.getCachedId(channel.getGuild().getID()) == replyPattern.id))
						TReplyPattern.delete(replyPattern);
					bot.loadConfiguration();
					return Template.get("command_autoreply_deleted", args[1]);
				case "regex":
				case "pattern":
					try {
						Pattern pattern = Pattern.compile(restOfArgs);//used to see if a patterns is valid, invalid = exception ;)
						replyPattern.pattern = restOfArgs;
						TReplyPattern.update(replyPattern);
					} catch (PatternSyntaxException exception) {
						return Template.get("command_autoreply_regex_invalid") + Config.EOL +
								exception.getDescription() + Config.EOL +
								Misc.makeTable(exception.getMessage());
					}
					return Template.get("command_autoreply_regex_saved");
				case "guild":
				case "gid":
					if (!bot.isCreator(author)) {
						return Template.get("no_permission");
					}
					if (!args[2].equals("0")) {
						OServer server = TServers.findBy(args[2]);
						if (server.id == 0) {
							return Template.get("command_autoreply_guild_invalid", args[2]);
						}
						replyPattern.guildId = server.id;
					} else {
						replyPattern.guildId = 0;
					}
					TReplyPattern.update(replyPattern);
					return Template.get("command_autoreply_guild_saved", args[2]);
				case "response":
				case "reply":
					replyPattern.reply = EmojiParser.parseToAliases(restOfArgs);
					TReplyPattern.update(replyPattern);
					return Template.get("command_autoreply_response_saved");
				case "tag":
					replyPattern.tag = args[2];
					TReplyPattern.update(replyPattern);
					return Template.get("command_autoreply_tag_saved");
				case "cd":
				case "cooldown":
					replyPattern.cooldown = Long.parseLong(args[2]);
					TReplyPattern.update(replyPattern);
					return Template.get("command_autoreply_cooldown_saved");
				case "test":
					Pattern pattern = Pattern.compile(replyPattern.pattern);
					Matcher matcher = pattern.matcher(restOfArgs);
					if (matcher.find()) {
//						return String.format("`%s` matches `%s`", restOfArgs, replyPattern.pattern);
						return replyPattern.reply;
					}
					return Template.get("command_autoreply_no_match");
				default:
					return Template.get("invalid_use");
			}
		}
		if (args.length == 1) {
			OReplyPattern replyPattern = TReplyPattern.findBy(args[0]);
			if (replyPattern.id == 0) {
				return Template.get("command_autoreply_not_exists", args[0]);
			}
			List<List<String>> tbl = new ArrayList<>();
			tbl.add(Arrays.asList("created on ", "" + replyPattern.createdOn));
			tbl.add(Arrays.asList("tag", replyPattern.tag));
			tbl.add(Arrays.asList("creator", "" + replyPattern.userId));
			tbl.add(Arrays.asList("guild", "" + replyPattern.guildId));
			tbl.add(Arrays.asList("pattern", "" + replyPattern.pattern));
			tbl.add(Arrays.asList("reply", "" + replyPattern.reply));
			tbl.add(Arrays.asList("cooldown", "" + TimeUtil.getRelativeTime((System.currentTimeMillis() + replyPattern.cooldown + 500L) / 1000L, false, false)));
			return "Auto reply information for `" + args[0] + "`:" + Misc.makeAsciiTable(Arrays.asList("Property", "Value"), tbl);
		}
		return Template.get("invalid_use");
	}
}