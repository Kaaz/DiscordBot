package discordbot.command.music;

import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

/**
 * !playlist
 * shows the current songs in the queue
 */
public class Playlist extends AbstractCommand {

	public Playlist() {
		super();
	}

	@Override
	public String getDescription() {
		return "information about the playlists";
	}

	@Override
	public String getCommand() {
		return "playlist";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"playlist              //info about the current playlist",
				"playlist use mine     //use your playlist",
				"playlist use guild    //use the guild's playlist",
				"playlist use global   //use the global playlist",
				"playlist setting      //check the settings for the active playlist",
				"playlist list         //yes list list, see what playlists there are",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"pl"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
		if (args.length == 0) {
			int listId = player.getActivePLaylistId();
			if (listId == 0) {
				return "no playlist active at the moment, using the global list.";
			}
			return "";
		} else {
			switch (args[0].toLowerCase()) {
				case "use":
					break;
				case "setting":
					break;
			}
		}
		return Template.get("command_invalid_use");
	}
}