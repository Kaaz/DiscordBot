package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.awt.*;
import java.sql.Timestamp;

public class OModerationCase extends AbstractModel {
	public int guildId = 0;
	public int userId = 0;
	public int id = 0;
	public int moderatorId = 0;
	public long messageId = 0L;
	public Timestamp createdAt = null;
	public Timestamp expires = null;
	public PunishType punishment = PunishType.KICK;
	public String reason = "";

	public void setPunishment(int punishment) {
		this.punishment = PunishType.fromId(punishment);
	}

	public enum PunishType {
		WARN(1, "Warned", "Adds a strike to the user", new Color(0xFFF300)),
		MUTE(2, "Muted", "Actions are restricted", new Color(0xFFF300)),
		KICK(3, "Kicked", "Kicked from the guild", new Color(0xFF9600)),
		TMP_BAN(4, "temp-ban", "Kicked from the guild, unable to rejoin for a while", new Color(0xFF4700)),
		BAN(5, "banned", "permanently banned", new Color(0xB70000));

		private final int id;
		private final String keyword;
		private final String description;
		private final Color color;

		PunishType(int id, String keyword, String description, Color color) {
			this.id = id;
			this.keyword = keyword;
			this.description = description;
			this.color = color;
		}

		public static PunishType fromId(int id) {
			for (PunishType et : values()) {
				if (id == et.getId()) {
					return et;
				}
			}
			return KICK;
		}

		public int getId() {
			return id;
		}

		public String getDescription() {
			return description;
		}

		public String getKeyword() {
			return keyword;
		}

		public Color getColor() {
			return color;
		}
	}
}
