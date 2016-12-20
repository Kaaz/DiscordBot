package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OPlaylist extends AbstractModel {
	public int id = 0;
	public String title = "";
	public int ownerId = 0;
	public Timestamp createdOn = null;
	public int guildId = 0;
	private Visibility visibility = Visibility.GUILD;
	private EditType editType = EditType.PUBLIC_AUTO;
	private PlayType playType = PlayType.SHUFFLE;

	public boolean isGlobalList() {
		return id > 0 && ownerId == 0 && guildId == 0;
	}

	public boolean isGuildList() {
		return id > 0 && guildId > 0 && ownerId == 0;
	}

	public boolean isPersonal() {
		return id > 0 && guildId == 0 && ownerId > 0;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibilityId) {
		this.visibility = Visibility.fromId(visibilityId);
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public EditType getEditType() {
		return editType;
	}

	public void setEditType(int editId) {
		this.editType = EditType.fromId(editId);
	}

	public void setEditType(EditType editType) {
		this.editType = editType;
	}

	public PlayType getPlayType() {
		return playType;
	}

	public void setPlayType(PlayType playType) {
		this.playType = playType;
	}

	public void setPlayType(int id) {
		setPlayType(PlayType.fromId(id));
	}


	public enum Visibility {
		UNKNOWN(0, "??"),
		PUBLIC(1, "Anyone can see and use the playlist"),
		PUBLIC_USE(2, "Anyone can use the playlist"),
		GUILD(3, "only this guild can see/use the playlist"),
		PRIVATE(4, "only you/admins can see/use it");

		private final int id;
		private final String description;

		Visibility(int id, String description) {

			this.id = id;
			this.description = description;
		}

		public static Visibility fromId(int id) {
			for (Visibility vis : values()) {
				if (id == vis.getId()) {
					return vis;
				}
			}
			return UNKNOWN;
		}

		public int getId() {
			return id;
		}

		public String getDescription() {
			return description;
		}
	}

	public enum EditType {
		UNKNOWN(0, "??"),
		PUBLIC_AUTO(1, "all music played is automatically added"),
		PUBLIC_FULL(2, "Anyone can add and remove music from the playlist"),
		PUBLIC_ADD(3, "Anyone can add music, but not remove it"),
		PRIVATE_AUTO(4, "Music played by you/admins will be added automatically"),
		PRIVATE(5, "Only the owner/admin can add/remove music from the playlist manually ");

		private final int id;
		private final String description;

		EditType(int id, String description) {

			this.id = id;
			this.description = description;
		}

		public static EditType fromId(int id) {
			for (EditType et : values()) {
				if (id == et.getId()) {
					return et;
				}
			}
			return UNKNOWN;
		}

		public int getId() {
			return id;
		}

		public String getDescription() {
			return description;
		}
	}

	public enum PlayType {
		SHUFFLE(1, "Randomly selects the next track"),
		LOOP(2, "Iterates through the playlist");
		private final int id;
		private final String description;

		PlayType(int id, String description) {

			this.id = id;
			this.description = description;
		}

		public static PlayType fromId(int id) {
			for (PlayType et : values()) {
				if (id == et.getId()) {
					return et;
				}
			}
			return SHUFFLE;
		}

		public int getId() {
			return id;
		}

		public String getDescription() {
			return description;
		}
	}
}
