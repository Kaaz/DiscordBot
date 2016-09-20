package discordbot.db.model;

import discordbot.db.AbstractModel;

public class OCommandCooldown extends AbstractModel {
	public String command = "";
	public String targetId = "";
	public int targetType = 0;
	public long lastTime = 0L;
}
