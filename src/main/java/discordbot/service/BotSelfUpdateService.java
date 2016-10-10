package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.core.ExitCode;
import discordbot.guildsettings.defaults.SettingBotUpdateWarning;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.main.ProgramVersion;
import discordbot.util.UpdateUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Checks if there is an update for the bot and restarts if there is
 */
public class BotSelfUpdateService extends AbstractService {

	public BotSelfUpdateService(DiscordBot b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_self_update";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(30);
	}

	@Override
	public boolean shouldIRun() {
		return Config.BOT_AUTO_UPDATE;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		ProgramVersion latestVersion = UpdateUtil.getLatestVersion();
		if (latestVersion.isHigherThan(Launcher.getVersion())) {
			for (IChannel channel : getSubscribedChannels()) {
				bot.out.sendMessage(channel, String.format(Template.get("bot_self_update_restart"), Launcher.getVersion().toString(), latestVersion.toString()));
			}
			for (IGuild guild : bot.client.getGuilds()) {
				String announce = GuildSettings.get(guild).getOrDefault(SettingBotUpdateWarning.class);
				switch (announce.toLowerCase()) {
					case "off":
						continue;
					case "always":
						bot.out.sendMessage(bot.getDefaultChannel(guild), String.format(Template.get("bot_self_update_restart"), Launcher.getVersion().toString(), latestVersion.toString()));
						break;
					case "playing":
						for (IVoiceChannel voiceChannel : bot.client.getConnectedVoiceChannels()) {
							if (voiceChannel.getGuild().getID().equals(guild.getID())) {
								bot.out.sendMessage(bot.getMusicChannel(guild), String.format(Template.get("bot_self_update_restart"), Launcher.getVersion().toString(), latestVersion.toString()));
								break;
							}
						}
						break;
					default:
						break;
				}
			}
			bot.timer.schedule(
					new TimerTask() {
						@Override
						public void run() {
							Launcher.stop(ExitCode.UPDATE);
						}
					}
					, TimeUnit.MINUTES.toMillis(1));
		}
	}

	@Override
	public void afterRun() {
	}
}