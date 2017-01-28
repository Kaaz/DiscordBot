/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.command.music;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.vdurmont.emoji.EmojiParser;
import discordbot.command.CommandVisibility;
import discordbot.command.ICommandCleanup;
import discordbot.core.AbstractCommand;
import discordbot.db.controllers.CMusic;
import discordbot.db.controllers.CPlaylist;
import discordbot.db.controllers.CUser;
import discordbot.db.model.OMusic;
import discordbot.db.model.OPlaylist;
import discordbot.db.model.OUser;
import discordbot.guildsettings.music.SettingMusicRole;
import discordbot.handler.GuildSettings;
import discordbot.handler.MusicPlayerHandler;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.YTSearch;
import discordbot.util.YTUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * !play
 * plays a youtube link
 * yea.. play is probably not a good name at the moment
 */
public class PlayCommand extends AbstractCommand implements ICommandCleanup {
	private YTSearch ytSearch;

	public PlayCommand() {
		super();
		ytSearch = new YTSearch();
	}

	@Override
	public void cleanup() {
		ytSearch.resetCache();
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
		return new String[]{"p"};
	}

	private boolean isInVoiceWith(Guild guild, User author) {
		VoiceChannel channel = guild.getMember(author).getVoiceState().getChannel();
		if (channel == null) {
			return false;
		}
		for (Member user : channel.getMembers()) {
			if (user.getUser().getId().equals(guild.getJDA().getSelfUser().getId())) {
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

		if (!PermissionUtil.checkPermission(txt, guild.getSelfMember(), Permission.MESSAGE_WRITE)) {
			return "";
		}
		MusicPlayerHandler player = MusicPlayerHandler.getFor(guild, bot);
		if (!isInVoiceWith(guild, author)) {
			if (guild.getMember(author).getVoiceState().getChannel() == null) {
				return "you are not in a voicechannel";
			}
			try {
				if (player.isConnected()) {
					if (!userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
						return Template.get("music_not_same_voicechannel");
					}
					player.leave();
				}
				if (!PermissionUtil.checkPermission(guild.getMember(author).getVoiceState().getChannel(), guild.getSelfMember(), Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
					return Template.get("music_join_no_permission", guild.getMember(author).getVoiceState().getChannel().getName());
				}
				player.connectTo(guild.getMember(author).getVoiceState().getChannel());
			} catch (Exception e) {
				e.printStackTrace();
				return "Can't connect to you";
			}
		} else if (MusicPlayerHandler.getFor(guild, bot).getUsersInVoiceChannel().size() == 0) {
			return Template.get("music_no_users_in_channel");
		}
		if (args.length > 0) {
			final String videoTitle;
			String videoCode = YTUtil.extractCodeFromUrl(args[0]);
			String playlistCode = YTUtil.getPlayListCode(args[0]);
			if (playlistCode != null) {
				if (!ytSearch.hasValidKey()) {
					return Template.get("music_no_valid_youtube_key");
				}
				if (userRank.isAtLeast(SimpleRank.CONTRIBUTOR) || CUser.findBy(author.getId()).hasPermission(OUser.PermissionNode.IMPORT_PLAYLIST)) {
					List<YTSearch.SimpleResult> items = ytSearch.getPlayListItems(playlistCode);
					String output = "Added the following items to the playlist: " + Config.EOL;
					int playCount = 0;
					for (YTSearch.SimpleResult track : items) {
						if (playCount++ == Config.MUSIC_MAX_PLAYLIST_SIZE) {
							output += "Maximum of **" + Config.MUSIC_MAX_PLAYLIST_SIZE + "** items in the playlist!";
							break;
						}
						String out = handleFile(player, bot, (TextChannel) channel, author, track.getCode(), track.getTitle(), false);
						if (!out.isEmpty()) {
							output += out + Config.EOL;
						}
					}
					return output;
				}
			}
			if (!YTUtil.isValidYoutubeCode(videoCode)) {
				if (!ytSearch.hasValidKey()) {
					return Template.get("music_no_valid_youtube_key");
				}
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
				return handleFile(player, bot, (TextChannel) channel, author, videoCode, videoTitle, true);
			} else {
				return Template.get("command_play_no_results");
			}
		} else {
			if (player.isPlaying()) {
				if (player.isPaused()) {
					player.togglePause();
				}
				return "";
			}
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

	private String handleFile(MusicPlayerHandler player, DiscordBot bot, TextChannel channel, User invoker, String videoCode, String videoTitle, boolean useTemplates) {
		OMusic record = CMusic.findByYoutubeId(videoCode);
		final File filecheck;
		if (record.id > 0 && record.fileExists == 1) {
			filecheck = new File(record.filename);
		} else {
			filecheck = new File(YTUtil.getOutputPath(videoCode));
		}
		boolean isInProgress = bot.getContainer().isInProgress(videoCode);
		if (!filecheck.exists() && !isInProgress) {
			final String finalVideoCode = videoCode;
			bot.out.sendAsyncMessage(channel, Template.get("music_downloading_in_queue", videoTitle), message -> {
				bot.getContainer().downloadRequest(finalVideoCode, videoTitle, message, msg -> {
					try {
						File targetFile = new File(YTUtil.getOutputPath(videoCode));
						if (targetFile.exists()) {
							if (msg != null) {
								msg.editMessage(":notes: Found *" + videoTitle + "* And added it to the queue").queue();
							}
							player.addToQueue(targetFile.toPath().toRealPath().toString(), invoker);
						} else {
							if (msg != null) {
								msg.editMessage("Download failed, the song is likely too long or region locked!").queue();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
						if (msg != null) {
							msg.editMessage(Template.get("music_file_error")).queue();
						}
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
			player.addToQueue(path, invoker);
			if (useTemplates) {
				return Template.get("music_added_to_queue", rec.youtubeTitle);
			}
			return "\u25AA " + rec.youtubeTitle;
		} catch (Exception e) {
			bot.getContainer().reportError(e, "ytcode", videoCode);
			return Template.get("music_file_error");
		}
	}
}