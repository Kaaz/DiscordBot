package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bot restart service, designed so another bot can restart this one if necessary.
 *
 * @author nija123098
 */
public class BotRestartService extends AbstractService implements IListener<MessageReceivedEvent> {
	public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	private IDiscordClient client;
	Pattern shardPattern = Pattern.compile("emily restart shard\\s?(\\d+)");
	private long lastRestart;

	public BotRestartService(BotContainer b) {
		super(b);
		lastRestart = System.currentTimeMillis();
		try {
			client = new ClientBuilder().withToken(Config.BOT_RESTART_TOKEN).login();
//			client.getDispatcher().registerListener(this);
		} catch (Exception e) {
			LOGGER.error("Error starting Robot Restart Service!", e);
			System.exit(0);
		}
	}

	@Override
	public String getIdentifier() {
		return "bot_restart_service";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(1);
	}

	@Override
	public boolean shouldIRun() {
		return Config.BOT_RESTART_INACTIVE_SHARDS;
	}

	@Override
	public void beforeRun() {

	}

	@Override
	public void run() throws Exception {
		if (System.currentTimeMillis() > lastRestart + TimeUnit.MINUTES.toMillis(15)) {
//			RequestBuffer.request(() -> {
//				try {
//					this.client.logout();
//				} catch (DiscordException e) {
//					e.printStackTrace();
//				}
//			});
//			Thread.sleep(TimeUnit.SECONDS.toMillis(6));
//			RequestBuffer.request(() -> {
//				try {
//					this.client.login();
//				} catch (DiscordException e) {
//					e.printStackTrace();
//				}
//			});
			lastRestart = System.currentTimeMillis();
		}

	}

	@Override
	public void afterRun() {

	}

	@Override
	public void handle(MessageReceivedEvent event) {
		if (client.isReady()) {
			return;
		}
		String msg = event.getMessage().getContent().toLowerCase();
		IChannel channel = event.getMessage().getChannel();
		if (!msg.startsWith("emily restart")) {
			return;
		}
		if (!this.bot.getShardFor(event.getMessage().getGuild().getID()).security.isBotAdmin(event.getMessage().getAuthor().getID())) {
			return;
		}
		if (msg.startsWith("emily restart all")) {
			sendMessage(channel, "restarting all shards");
			DiscordBot[] discordBots = this.bot.getShards();
			for (int i = 0; i < discordBots.length; i++) {
				if (discordBots[i] == null || !discordBots[i].isReady()) {
					restart(i, false);
				}
			}
		} else {
			Matcher m = shardPattern.matcher(msg);
			if (m.find()) {
				int shardId = Misc.parseInt(m.group(1), -1);
				if (shardId < 0 || shardId >= bot.getShards().length) {
					return;
				}
				sendMessage(channel, "restarting shard \\#" + shardId);
				restart(shardId, false);
			}
		}
	}

	private void sendMessage(IChannel channel, String message) {
		RequestBuffer.request(() -> {
			try {
				channel.sendMessage(message);
			} catch (DiscordException | MissingPermissionsException ignored) {
			}
		});
	}

	private boolean restart(int shardId, boolean retry) {
		boolean success;
		int attemptsLeft = 3;
		do {
			success = bot.tryRestartingShard(shardId);
			attemptsLeft--;
		}
		while (!success && retry && attemptsLeft > 0);
		return success;
	}
}
