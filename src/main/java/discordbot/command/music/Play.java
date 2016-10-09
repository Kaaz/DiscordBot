package discordbot.command.music;

import com.google.common.base.Joiner;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.YTSearch;
import discordbot.util.YTUtil;
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
	YTSearch ytSearch;

	public Play(DiscordBot b) {
		super(b);
		ytSearch = new YTSearch(Config.GOOGLE_API_KEY);
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
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"play <youtubelink>    //download and plays song",
				"play <part of title>  //shows search results",
				"play                  //just start playing something"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (bot.client.getConnectedVoiceChannels().size() == 0) {
			return Template.get("music_not_in_voicechannel");
		}
		if (MusicPlayerHandler.getFor(channel.getGuild(), bot).getUsersInVoiceChannel().size() == 0) {
			return Template.get("music_no_users_in_channel");
		}
		if (args.length > 0) {
			boolean justDownloaded = false;

			String videocode = YTUtil.extractCodeFromUrl(args[0]);
			if (!YTUtil.isValidYoutubeCode(videocode)) {
				videocode = ytSearch.getResults(Joiner.on(" ").join(args));
			}
			if (YTUtil.isValidYoutubeCode(videocode)) {

				File filecheck = new File(YTUtil.getOutputPath(videocode));
				if (!filecheck.exists()) {
					IMessage msg = bot.out.sendMessage(channel, Template.get("music_downloading_hang_on"));
					if (YTUtil.downloadfromYoutubeAsMp3(videocode)) {
						bot.out.editMessage(msg, "resampling.. hang on");
						YTUtil.resampleToWav(videocode);
						justDownloaded = true;
					}
					try {
						msg.delete();
					} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
						e.printStackTrace();
					}
				}
				if (filecheck.exists()) {
					if (justDownloaded) {
						OMusic rec = TMusic.findByYoutubeId(videocode);
						rec.youtubeTitle = YTUtil.getTitleFromPage(videocode);
						rec.youtubecode = videocode;
						rec.filename = YTUtil.getOutputPath(videocode);
						TMusic.update(rec);
						bot.addSongToQueue(YTUtil.getOutputPath(videocode), channel.getGuild());
						return ":notes: Found *" + rec.youtubeTitle + "* And added it to the queue";
					}
					bot.addSongToQueue(YTUtil.getOutputPath(videocode), channel.getGuild());
					return Template.get("music_added_to_queue");
				}
			} else {

				return Template.get("command_play_no_results");

			}
		} else {
			if (bot.playRandomSong(channel.getGuild())) {
				return Template.get("music_started_playing_random");
			} else {
				return Template.get("music_failed_to_start");
			}
		}
		return Template.get("music_not_added_to_queue");
	}
}