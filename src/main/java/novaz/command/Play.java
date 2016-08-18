package novaz.command;

import novaz.core.AbstractCommand;
import novaz.db.model.OMusic;
import novaz.db.table.TMusic;
import novaz.handler.TextHandler;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.util.YTUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;

/**
 * !play
 * plays a youtube link
 * yea.. play is probably not a good name at the moment
 */
public class Play extends AbstractCommand {

	public Play(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Plays a song from youtube";
	}

	@Override
	public String getCommand() {
		return "play";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"play <youtubelink>",
				"play <youtubevideocode>"};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			boolean justDownloaded = false;
			String videocode = YTUtil.extractCodeFromUrl(args[0]);
			if (YTUtil.isValidYoutubeCode(videocode)) {
				File filecheck = new File(Config.MUSIC_DIRECTORY + videocode + ".mp3");
				if (!filecheck.exists()) {
					IMessage msg = bot.sendMessage(channel, TextHandler.get("music_downloading_hang_on"));
					YTUtil.downloadfromYoutubeAsMp3(videocode);
					justDownloaded = true;
					try {
						msg.delete();
					} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
						e.printStackTrace();
					}
				}
				if (filecheck.exists()) {
					if (justDownloaded) {
						OMusic rec = TMusic.findByYoutubeId(videocode);
						rec.title = YTUtil.getTitleFromPage(videocode);
						rec.youtubecode = videocode;
						rec.filename = videocode + ".mp3";
						TMusic.update(rec);
						bot.addSongToQueue(videocode + ".mp3", channel.getGuild());
						return ":notes: Found *" + rec.title + "* And added it to the queue";
					}
					bot.addSongToQueue(videocode + ".mp3", channel.getGuild());
					return TextHandler.get("music_added_to_queue");
				}
			}
		} else {
			if (bot.playRandomSong(channel.getGuild())) {
				return TextHandler.get("music_started_playing_random");
			} else {
				return TextHandler.get("music_failed_to_start");
			}
		}
		return TextHandler.get("music_not_added_to_queue");
	}
}