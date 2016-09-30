package discordbot.command.fun;

import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OTag;
import discordbot.db.table.TServers;
import discordbot.db.table.TTag;
import discordbot.db.table.TUser;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * !tag
 */
public class TagCommand extends AbstractCommand {

	public TagCommand(DiscordBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Tags!";
	}

	@Override
	public String getCommand() {
		return "tag";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"tag                  //list of tags",
				"tag <name>           //shows the tag",
				"tag mine             //shows your tags",
				"tag list             //shows all tags ",
				"tag delete <name>    //deletes tag",
				"tag <name> <content> //creates the tag",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"t"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length == 0 || args[0].equals("list")) {
			List<OTag> tags = TTag.getTagsFor(channel.getGuild().getID());
			if (tags.isEmpty()) {
				return Template.get("command_tag_no_tags");
			}
			return "The following tags exist: " + Config.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
		} else if (args[0].equalsIgnoreCase("mine")) {
			List<OTag> tags = TTag.getTagsFor(channel.getGuild().getID(), author.getID());
			if (tags.isEmpty()) {
				return Template.get("command_tag_no_tags");
			}
			return "You have made the following tags: " + Config.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
		}
		channel.getGuild().getRolesForUser(author);
		OTag tag = TTag.findBy(channel.getGuild().getID(), args[0]);
		if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
			if (tag.id > 0) {
				if (!bot.isAdmin(channel, author) && TUser.getCachedId(author.getID()) != tag.userId) {
					return Template.get("command_tag_only_delete_own");
				}
				TTag.delete(tag);
				return Template.get("command_tag_delete_success");
			}
			return Template.get("command_tag_nothing_to_delete");
		} else if (args.length > 1) {
			if (tag.id > 0 && tag.userId != TUser.getCachedId(author.getID())) {

				return Template.get("command_tag_only_creator_can_edit");
			}
			String output = "";
			for (int i = 1; i < args.length; i++) {
				output += " " + args[i];
			}
			output = output.trim();
			if (tag.id == 0) {
				tag.tagname = args[0];
				tag.guildId = TServers.getCachedId(channel.getGuild().getID());
				tag.userId = TUser.getCachedId(author.getID());
				tag.created = new Timestamp(System.currentTimeMillis());
			}
			tag.response = EmojiParser.parseToAliases(output);
			if (tag.response.length() > 2000) {
				tag.response = tag.response.substring(0, 1999);
			}
			TTag.insert(tag);
			return Template.get("command_tag_saved");
		}
		if (tag.id > 0) {
			return tag.response;

		}
		return Template.get("command_tag_not_set");
	}
}