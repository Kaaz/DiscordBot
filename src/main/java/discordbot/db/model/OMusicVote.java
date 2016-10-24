package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OMusicVote extends AbstractModel {
	public int songId = 0;
	public int userId = 0;
	public int vote = 0;
	public Timestamp createdOn = null;
}
