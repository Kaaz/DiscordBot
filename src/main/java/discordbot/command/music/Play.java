package discordbot.command.music;

import com.google.common.base.Joiner;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.guildsettings.defaults.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.YTSearch;
import discordbot.util.YTUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.utils.PermissionUtil;

import java.io.File;
import java.io.IOException;

/**
 * !play
 * plays a youtube link
 * yea.. play is probably not a good name at the moment
 */
public class Play extends AbstractCommand {
	private YTSearch ytSearch;

	public Play() {
		super();
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
				"play                  //just start playing something",
				"play role <role>      //you need this role in order to play music"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[0];
	}

	private boolean isInVoiceWith(Guild guild, User author) {
		VoiceChannel channel = guild.getVoiceStatusOfUser(author).getChannel();
		if (channel == null) {
			return false;
		}
		for (User user : channel.getUsers()) {
			if (user.getId().equals(guild.getJDA().getSelfInfo().getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		TextChannel txt = (TextChannel) channel;
		Guild guild = txt.getGuild();
		SimpleRank simpleRank = bot.security.getSimpleRank(author, txt);
		String rolerequirement = GuildSettings.getFor(channel, SettingMusicRole.class);
		if (!"none".equals(rolerequirement)) {
				
		}
		if (!PermissionUtil.checkPermission(txt, bot.client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
			return "";
		}
		if (!isInVoiceWith(guild, author)) {
			bot.connectTo(guild.getVoiceStatusOfUser(author).getChannel());
			try {
				Thread.sleep(2000L);// ¯\_(ツ)_/¯
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (bot.isConnectedTo(guild.getVoiceStatusOfUser(author).getChannel())) {
				return "can't connect to you";
			}
		} else if (MusicPlayerHandler.getFor(guild, bot).getUsersInVoiceChannel().size() == 0) {
			return Template.get("music_no_users_in_channel");
		}
		if (args.length > 0) {

			String videocode = YTUtil.extractCodeFromUrl(args[0]);
			if (!YTUtil.isValidYoutubeCode(videocode)) {
				videocode = ytSearch.getResults(Joiner.on(" ").join(args));
			}
			if (YTUtil.isValidYoutubeCode(videocode)) {
				try {
					final File filecheck = new File(YTUtil.getOutputPath(videocode));
					if (!filecheck.exists()) {
						String finalVideocode = videocode;
						bot.out.sendAsyncMessage(channel, Template.get("music_downloading_hang_on"), message -> {
							System.out.println("starting download with code:::::" + finalVideocode);
							YTUtil.downloadfromYoutubeAsMp3(finalVideocode);
							try {
								if (filecheck.exists()) {
									String path = filecheck.toPath().toRealPath().toString();
									OMusic rec = TMusic.findByYoutubeId(finalVideocode);
									rec.youtubeTitle = YTUtil.getTitleFromPage(finalVideocode);
									rec.youtubecode = finalVideocode;
									rec.filename = path;
									TMusic.update(rec);
									message.updateMessageAsync(":notes: Found *" + rec.youtubeTitle + "* And added it to the queue", null);
									bot.addSongToQueue(path, guild);
								} else {
									message.updateMessageAsync("Download failed, the song is most likely too long!", null);
								}
							} catch (Exception e) {
								bot.out.sendErrorToMe(e);
							}

						});

						return "";
					} else if (filecheck.exists()) {
						String path = filecheck.toPath().toRealPath().toString();
						bot.addSongToQueue(path, guild);
						return Template.get("music_added_to_queue");
					}
				} catch (IOException e) {
					bot.out.sendErrorToMe(e);
				}
			} else {

				return Template.get("command_play_no_results");

			}
		} else {
			if (bot.playRandomSong(guild)) {
				return Template.get("music_started_playing_random");
			} else {
				return Template.get("music_failed_to_start");
			}
		}
		return Template.get("music_not_added_to_queue");
	}
}