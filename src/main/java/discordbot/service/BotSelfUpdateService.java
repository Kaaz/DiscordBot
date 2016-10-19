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
		DiscordBot bot = this.bot.getShards()[0];
		if (latestVersion.isHigherThan(Launcher.getVersion())) {
			usersHaveBeenWarned = true;
			for (TextChannel channel : getSubscribedChannels()) {
				sendTo(channel, Template.get("bot_self_update_restart", Launcher.getVersion().toString(), latestVersion.toString()));
			}
			for (DiscordBot discordBot : this.bot.getShards()) {
				for (Guild guild : discordBot.client.getGuilds()) {
					String announce = GuildSettings.get(guild).getOrDefault(SettingBotUpdateWarning.class);
					switch (announce.toLowerCase()) {
						case "off":
							continue;
						case "always":
							discordBot.out.sendAsyncMessage(bot.getDefaultChannel(guild), Template.get("bot_self_update_restart", Launcher.getVersion().toString(), latestVersion.toString()), null);
							break;
						case "playing":
							if (guild.getAudioManager().isConnected()) {
								discordBot.out.sendAsyncMessage(discordBot.getMusicChannel(guild), Template.get("bot_self_update_restart", Launcher.getVersion().toString(), latestVersion.toString()), null);
							}
							break;
						default:
							break;
					}
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