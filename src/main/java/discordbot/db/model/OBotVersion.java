package discordbot.db.model;

import discordbot.main.ProgramVersion;

import java.sql.Timestamp;

public class OBotVersion {

	public int id = 0;
	public int major = 1;
	public int minor = 0;
	public int patch = 0;
	public Timestamp createdOn = null;
	public int published = 0;

	public ProgramVersion getVersion() {
		return new ProgramVersion(major, minor, patch);
	}
}
