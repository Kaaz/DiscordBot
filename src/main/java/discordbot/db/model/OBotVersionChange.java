package discordbot.db.model;

import discordbot.util.Emojibet;

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
		ADDED(1, "A", "Added", Emojibet.CHECK_BOX),
		CHANGED(2, "C", "Changed", Emojibet.WRENCH),
		REMOVED(3, "R", "Removed", Emojibet.BASKET),
		FIXED(4, "F", "Bugs fixed", Emojibet.BUG),
		UNKNOWN(5, "?", "Misc", Emojibet.QUESTION_MARK);

		private final int id;
		private final String title;
		private final String code;
		private final String emoji;

		public String getEmoji() {
			return emoji;
		}

		ChangeType(int id, String code, String title, String emoji) {
			this.title = title;
			this.id = id;
			this.code = code;
			this.emoji = emoji;
		}

		public static ChangeType fromId(int id) {
			for (ChangeType et : values()) {
				if (id == et.getId()) {
					return et;
				}
			}
			return UNKNOWN;
		}

		public static ChangeType fromCode(String code) {
			for (ChangeType et : values()) {
				if (code.equalsIgnoreCase(et.getCode())) {
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
