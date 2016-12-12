package discordbot.command.fun;

import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CGuild;
import discordbot.db.controllers.CTag;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OTag;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * !tag
 */
public class TagCommand extends AbstractCommand {

	public TagCommand() {
		super();
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
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		SimpleRank rank = bot.security.getSimpleRank(author, channel);
		if (args.length == 0 || args[0].equals("list")) {
			List<OTag> tags = CTag.getTagsFor(guild.getId());
			if (tags.isEmpty()) {
				return Template.get("command_tag_no_tags");
			}
			return "The following tags exist: " + Config.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
		} else if (args[0].equalsIgnoreCase("mine")) {
			List<OTag> tags = CTag.getTagsFor(guild.getId(), author.getId());
			if (tags.isEmpty()) {
				return Template.get("command_tag_no_tags");
			}
			return "You have made the following tags: " + Config.EOL + Misc.makeTable(tags.stream().map(sc -> sc.tagname).collect(Collectors.toList()));
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
			OTag tag = CTag.findBy(guild.getId(), args[1]);
			if (tag.id > 0) {
				if (!rank.isAtLeast(SimpleRank.GUILD_ADMIN) && CUser.getCachedId(author.getId()) != tag.userId) {
					return Template.get("command_tag_only_delete_own");
				}
				CTag.delete(tag);
				return Template.get("command_tag_delete_success");
			}
			return Template.get("command_tag_nothing_to_delete");
		}
		OTag tag = CTag.findBy(guild.getId(), args[0]);
		if (args.length > 1) {
			if (tag.id > 0 && tag.userId != CUser.getCachedId(author.getId())) {
				return Template.get("command_tag_only_creator_can_edit");
			}
			String output = "";
			for (int i = 1; i < args.length; i++) {
				output += " " + args[i];
			}
			output = output.trim();
			if (tag.id == 0) {
				tag.tagname = args[0];
				tag.guildId = CGuild.getCachedId(guild.getId());
				tag.userId = CUser.getCachedId(author.getId(), author.getName());
				tag.created = new Timestamp(System.currentTimeMillis());
			}
			tag.response = EmojiParser.parseToAliases(output);
			if (tag.response.length() > 2000) {
				tag.response = tag.response.substring(0, 1999);
			}
			CTag.insert(tag);
			return Template.get("command_tag_saved");
		}
		if (tag.id > 0) {
			return tag.response;

		}
		return Template.get("command_tag_not_set");
	}
}