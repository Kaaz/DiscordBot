package discordbot.command.fun;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OTag;
import discordbot.db.table.TServers;
import discordbot.db.table.TTag;
import discordbot.db.table.TUser;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Timestamp;

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
				"tag                   //list of tags",
				"tag <name>            //shows the tag",
				"tag <name> <content>  //creates the tag",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length == 0) {
			return Template.get("not_implemented_yet");
		}
		OTag tag = TTag.findBy(channel.getGuild().getID(), args[0]);
		if (args.length > 1) {
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
			tag.response = output;
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