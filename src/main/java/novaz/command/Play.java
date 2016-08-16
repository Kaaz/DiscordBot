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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * !play
 * plays a file/url
 */
public class Play extends AbstractCommand {
	Pattern yturl = Pattern.compile("^.*((youtu.be/)|(v/)|(/u/\\w/)|(embed/)|(watch\\?))\\\\??v?=?([^#\\\\&\\?]*).*");

	public Play(NovaBot b) {
		super(b);
		setCmd("play");
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length > 0) {
			boolean justDownloaded = false;
			String videocode = extractvideocodefromyoutubeurl(args[0]);
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
		return TextHandler.get("music_not_added_to_queue");
	}

	private String extractvideocodefromyoutubeurl(String url) {
		Matcher matcher = yturl.matcher(url);
		if (matcher.find()) {
			return matcher.group(7);
		}
		return url;
	}
}