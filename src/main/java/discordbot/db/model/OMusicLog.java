package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OMusicLog extends AbstractModel {
	public int id = 0;
	public int musicId = 0;
	public int guildId = 0;
	public int userId = 0;

	public Timestamp playDate = null;
}
