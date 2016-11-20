package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OBotEvent extends AbstractModel {
	public int id = 0;
	public Timestamp createdOn = null;
	public String group = "";
	public String subGroup = "";
	public String data = "";
	public Level logLevel = Level.INFO;

	public enum Level {
		FATAL(1),
		ERROR(2),
		WARN(3),
		INFO(4),
		DEBUG(5),
		TRACE(6);

		private final int id;

		Level(int id) {

			this.id = id;
		}

		public static Level fromId(int id) {
			for (Level et : values()) {
				if (id == et.getId()) {
					return et;
				}
			}
			return INFO;
		}

		public int getId() {
			return id;
		}
	}
}
