package novaz.db.table;

import novaz.core.Logger;
import novaz.db.WebDb;
import novaz.db.model.OMusic;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TMusic {
	public static OMusic findByYoutubeId(String youtubeCode) {
		OMusic music = new OMusic();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, youtubecode, filename, title, artist, lastplaydate, banned  " +
						"FROM playlist " +
						"WHERE youtubecode = ? ", youtubeCode)) {
			music = fillRecord(rs);
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return music;
	}

	public static OMusic findByFileName(String filename) {
		OMusic music = new OMusic();
		try (ResultSet rs = WebDb.get().select(
				"SELECT id, youtubecode, filename, title, artist, lastplaydate, banned  " +
						"FROM playlist " +
						"WHERE filename = ? ", filename)) {
			music = fillRecord(rs);
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return music;
	}


	private static OMusic fillRecord(ResultSet resultset) throws SQLException {
		OMusic music = new OMusic();
		if (resultset.next()) {
			music.id = resultset.getInt("id");
			music.youtubecode = resultset.getString("youtubecode");
			music.filename = resultset.getString("filename");
			music.title = resultset.getString("title");
			music.artist = resultset.getString("artist");
			music.lastplaydate = resultset.getLong("lastplaydate");
			music.banned = resultset.getInt("banned");
		}
		return music;
	}

	public static void update(OMusic record) {
		if (record.id == 0) {
			insert(record);
			return;
		}
		try {
			WebDb.get().query(
					"UPDATE playlist SET  youtubecode = ?, filename = ?, title = ?, artist = ?, lastplaydate = ?, banned = ? " +
							"WHERE id = ? ",
					record.youtubecode, record.filename, record.title, record.artist, record.lastplaydate, record.banned, record.id
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insert(OMusic record) {
		try {
			record.id = WebDb.get().insert(
					"INSERT INTO playlist(youtubecode, filename, title, artist, lastplaydate, banned) " +
							"VALUES (?,?,?,?,?,?)",
					record.youtubecode, record.filename, record.title, record.artist, record.lastplaydate, record.banned);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
