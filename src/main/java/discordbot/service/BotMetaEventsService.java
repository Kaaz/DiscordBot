package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.db.model.OBotEvent;
import discordbot.db.table.TBotEvent;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import net.dv8tion.jda.entities.TextChannel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Bot meta events
 */
public class BotMetaEventsService extends AbstractService {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	public BotMetaEventsService(BotContainer b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_meta_events";
	}

	@Override
	public long getDelayBetweenRuns() {
		return TimeUnit.MINUTES.toMillis(5);
	}

	@Override
	public boolean shouldIRun() {
		return true;
	}

	@Override
	public void beforeRun() {
	}

	@Override
	public void run() {
		int lastId = Integer.parseInt("0" + getData("last_broadcast_id"));
		List<OBotEvent> events = TBotEvent.getEventsAfter(lastId);
		List<TextChannel> subscribedChannels = getSubscribedChannels();
		if (events.isEmpty()) {
			return;
		}
		for (OBotEvent event : events) {
			String output = String.format("[**%s**] [**%s**] [**%s**]: %s" + Config.EOL, dateFormat.format(event.createdOn), event.group, event.subGroup, event.data);
			for (TextChannel channel : subscribedChannels) {
				channel.sendMessageAsync(output, null);
			}
			lastId = event.id;
		}
		saveData("last_broadcast_id", lastId);
	}

	@Override
	public void afterRun() {
	}
}