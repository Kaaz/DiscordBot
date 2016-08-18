package novaz.command;

import novaz.core.AbstractCommand;
import novaz.db.model.OMusic;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * !current
 * retrieves information about the currently playing track
 */
public class CurrentTrack extends AbstractCommand {
	public CurrentTrack(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "retrieves information about the song currently playing";
	}

	@Override
	public String getCommand() {
		return "current";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		OMusic song = bot.getCurrentlyPlayingSong(channel.getGuild());
		if (song.id == 0) {
			return TextHandler.get("command_currentlyplaying_nosong");
		}
		String ret = ":notes: Song info " + Config.EOL;
		ret += "**Title** " + Config.EOL;
		ret += song.title + Config.EOL;
//		ret += song.youtubecode;
		List<IUser> userlist = bot.getCurrentlyListening(channel.getGuild());
		if (userlist.size() > 0) {
			ret += "Currently Listening: " + Config.EOL;
			ArrayList<String> displayList = userlist.stream().map(IUser::getName).collect(Collectors.toCollection(ArrayList::new));
			ret += Misc.makeTable(displayList);
		}

		return ret;
	}
}
