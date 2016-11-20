package discordbot.command.music;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CMusic;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.model.OMusic;
import discordbot.db.model.OPlaylist;
import discordbot.guildsettings.music.SettingMusicRole;
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
		return new String[]{"music", "p", "m"};
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
		SimpleRank userRank = bot.security.getSimpleRank(author, channel);
		if (!GuildSettings.get(guild).canUseMusicCommands(author, userRank)) {
			return Template.get(channel, "music_required_role_not_found", GuildSettings.getFor(channel, SettingMusicRole.class));
		}

		if (!PermissionUtil.checkPermission(txt, bot.client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
			return "";
		}
		if (!isInVoiceWith(guild, author)) {
			if (guild.getVoiceStatusOfUser(author).getChannel() == null) {
				return "you are not in a voicechannel";
			}
			try {
				bot.connectTo(guild.getVoiceStatusOfUser(author).getChannel());
				Thread.sleep(2000L);// ¯\_(ツ)_/¯
			} catch (Exception e) {
				e.printStackTrace();
				return "Can't connect to you";
			}
			if (bot.isConnectedTo(guild.getVoiceStatusOfUser(author).getChannel())) {
				return "can't connect to you";
			}
		} else if (MusicPlayerHandler.getFor(guild, bot).getUsersInVoiceChannel().size() == 0) {
			return Template.get("music_no_users_in_channel");
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
		if (args.length > 0) {
			final String videoTitle;
			String videoCode = YTUtil.extractCodeFromUrl(args[0]);
			if (!YTUtil.isValidYoutubeCode(videoCode)) {
				YTSearch.SimpleResult results = ytSearch.getResults(Joiner.on(" ").join(args));
				if (results != null) {
					videoCode = results.getCode();
					videoTitle = EmojiParser.parseToAliases(results.getTitle());
				} else {
					videoCode = null;
					videoTitle = "";
				}
			} else {
				videoTitle = videoCode;
			}
			if (videoCode != null && YTUtil.isValidYoutubeCode(videoCode)) {
				final File filecheck = new File(YTUtil.getOutputPath(videoCode));
				boolean isInProgress = bot.getContainer().isInProgress(videoCode);
				if (!filecheck.exists() && !isInProgress) {
					final String finalVideoCode = videoCode;
					bot.out.sendAsyncMessage(channel, Template.get("music_downloading_in_queue", videoTitle), message -> {
						bot.getContainer().downloadRequest(finalVideoCode, videoTitle, message, message1 -> {
							try {
								if (filecheck.exists()) {
									String path = filecheck.toPath().toRealPath().toString();
									OMusic rec = CMusic.findByYoutubeId(finalVideoCode);
									rec.youtubeTitle = (!videoTitle.isEmpty() && !videoTitle.equals(finalVideoCode)) ? videoTitle : EmojiParser.parseToAliases(YTUtil.getTitleFromPage(finalVideoCode));
									rec.youtubecode = finalVideoCode;
									rec.filename = path;
									rec.playCount += 1;
									rec.lastManualPlaydate = System.currentTimeMillis() / 1000L;
									CMusic.update(rec);
									message1.updateMessageAsync(":notes: Found *" + rec.youtubeTitle + "* And added it to the queue", null);
									player.addToQueue(path, author);
								} else {
									message1.updateMessageAsync("Download failed, the song is likely too long or region locked!", null);
								}
							} catch (IOException e) {
								e.printStackTrace();
								message1.updateMessageAsync(Template.get("music_file_error"), null);
							}
						});
					});
					return "";
				} else if (YTUtil.isValidYoutubeCode(videoCode) && isInProgress) {
					return Template.get(channel, "music_downloading_in_progress", videoTitle);
				}
				try {
					String path = filecheck.toPath().toRealPath().toString();
					OMusic rec = CMusic.findByFileName(path);
					CMusic.registerPlayRequest(rec.id);
					player.addToQueue(path, author);
					return Template.get("music_added_to_queue", rec.youtubeTitle);
				} catch (Exception e) {
					bot.out.sendErrorToMe(e, "ytcode", videoCode);
					return Template.get("music_file_error");
				}
			} else {
				return Template.get("command_play_no_results");
			}
		} else {
			if (player.playRandomSong()) {
				return Template.get("music_started_playing_random");
			} else {
				OPlaylist pl = CPlaylist.findById(player.getActivePLaylistId());
				if (!pl.isGlobalList()) {
					if (CPlaylist.getMusicCount(pl.id) == 0) {
						return Template.get("music_failed_playlist_empty", pl.title);
					}
				}
				return Template.get("music_failed_to_start");
			}
		}
	}
}