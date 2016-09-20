package discordbot.db.model;

import discordbot.db.AbstractModel;

public class OSubscription extends AbstractModel {
	public int serverId = 0;
	public int channelId = 0;
	public int serviceId = 0;
	public int subscribed = 0;
}
