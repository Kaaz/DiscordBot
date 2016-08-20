package novaz.command.music;

import novaz.core.AbstractCommand;
import novaz.db.model.OMusic;
import novaz.handler.MusicPlayerHandler;
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
	private final String BLOCK_INACTIVE = "▬";
	private final String BLOCK_ACTIVE = ":radio_button:";
	private final String SOUND_CHILL = ":sound:";
	private final String SOUND_LOUD = ":loud_sound:";
	private final float SOUND_TRESHHOLD = 0.4F;

	private final int BLOCK_PARTS = 10;

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
		String ret = "Currently playing " + ":notes: " + song.title + Config.EOL + Config.EOL;
		MusicPlayerHandler musicHandler = MusicPlayerHandler.getAudioPlayerForGuild(channel.getGuild(), bot);
		ret += getMediaplayerProgressbar(musicHandler.getCurrentSongStartTime(), musicHandler.getCurrentSongLength(), musicHandler.getVolume()) + Config.EOL + Config.EOL;
		List<IUser> userlist = bot.getCurrentlyListening(channel.getGuild());
		if (userlist.size() > 0) {
			ret += ":headphones:  Listeners" + Config.EOL;
			ArrayList<String> displayList = userlist.stream().map(IUser::getName).collect(Collectors.toCollection(ArrayList::new));
			ret += Misc.makeTable(displayList);
		}
		return ret;
	}

	/**
	 * @param startTime timestamp (in seconds) of the moment the song started playing
	 * @param duration  current song length in seconds
	 * @param volume    volume of the player
	 * @return a formatted mediaplayer
	 */
	private String getMediaplayerProgressbar(long startTime, long duration, float volume) {
		long current = System.currentTimeMillis() / 1000 - startTime;
		String bar = ":pause_button: ";
		int activeBLock = (int) ((float) current / (float) duration * (float) BLOCK_PARTS);
		for (int i = 0; i < BLOCK_PARTS; i++) {
			if (i == activeBLock) {
				bar += BLOCK_ACTIVE;
			} else {
				bar += BLOCK_INACTIVE;
			}
		}
		bar += " [" + Misc.getDurationString(current) + "/" + Misc.getDurationString(duration) + "] ";
		if (volume >= SOUND_TRESHHOLD) {
			bar += SOUND_LOUD;
		} else {
			bar += SOUND_CHILL;
		}
		return bar;
	}
}