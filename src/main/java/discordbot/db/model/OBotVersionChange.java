package discordbot.db.model;

public class OBotVersionChange {

	public int id = 0;
	public int author = 0;
	public String description = "";
	public int version = 0;
	public ChangeType changeType = ChangeType.UNKNOWN;

	public void setChangeType(int changeType) {
		this.changeType = ChangeType.fromId(changeType);
	}

	public enum ChangeType {
		ADDED(1, "A", "Added"),
		CHANGED(2, "C", "Changed"),
		REMOVED(3, "R", "Removed"),
		FIXED(4, "F", "Bugs fixed"),
		UNKNOWN(0, "?", "Misc");

		private final int id;
		private final String title;
		private final String code;

		public String getEmoji() {
			return "";
		}

		ChangeType(int id, String code, String title) {
			this.title = title;
			this.id = id;
			this.code = code;
		}

		public static ChangeType fromId(int id) {
			for (ChangeType et : values()) {
				if (id == et.getId()) {
					return et;
				}
			}
			return UNKNOWN;
		}

		public int getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getCode() {
			return code;
		}
	}
}
