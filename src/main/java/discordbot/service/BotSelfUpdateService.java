package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.core.ExitCode;
import discordbot.guildsettings.defaults.SettingBotUpdateWarning;
import discordbot.handler.GuildSettings;
import discordbot.handler.Template;
import discordbot.main.*;
import discordbot.util.UpdateUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Checks if there is an update for the bot and restarts if there is
 */
public class BotSelfUpdateService extends AbstractService {

	private boolean usersHaveBeenWarned = false;

	public BotSelfUpdateService(BotContainer b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_self_update";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(2);
	}

	@Override
	public boolean shouldIRun() {
		return Config.BOT_AUTO_UPDATE && !usersHaveBeenWarned;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		ProgramVersion latestVersion = UpdateUtil.getLatestVersion();
		DiscordBot firstShard = bot.getShards()[0];
		if (latestVersion.isHigherThan(Launcher.getVersion()) || bot.isTerminationRequested()) {
			firstShard.timer.schedule(
					new TimerTask() {
						@Override
						public void run() {
							if (latestVersion.isHigherThan(Launcher.getVersion())) {
								Launcher.stop(ExitCode.UPDATE);
							} else if (bot.isTerminationRequested()) {
								Launcher.stop(bot.getRebootReason());
							} else {
								Launcher.stop(ExitCode.NEED_MORE_SHARDS);
							}
						}
					}, TimeUnit.MINUTES.toMillis(1));
			usersHaveBeenWarned = true;
			String message = Template.get("announce_reboot");
			if (latestVersion.isHigherThan(Launcher.getVersion())) {
				message = Template.get("bot_self_update_restart", Launcher.getVersion().toString(), latestVersion.toString());
			} else if (bot.isTerminationRequested()) {
				switch (bot.getRebootReason()) {
					case NEED_MORE_SHARDS:
						message = Template.get("bot_reboot_more_shards");
						break;
					default:
						message = Template.get("announce_reboot");
				}
			}
			for (TextChannel channel : getSubscribedChannels()) {
				sendTo(channel, message);
			}
			for (DiscordBot discordBot : this.bot.getShards()) {
				for (Guild guild : discordBot.client.getGuilds()) {
					String announce = GuildSettings.get(guild).getOrDefault(SettingBotUpdateWarning.class);
					switch (announce.toLowerCase()) {
						case "off":
							continue;
						case "always":
							discordBot.out.sendAsyncMessage(discordBot.getDefaultChannel(guild), message, null);
							break;
						case "playing":
							if (guild.getAudioManager().isConnected()) {
								discordBot.out.sendAsyncMessage(discordBot.getMusicChannel(guild), message, null);
							}
							break;
						default:
							break;
					}
				}
			}
		}
	}

	@Override
	public void afterRun() {
	}
}