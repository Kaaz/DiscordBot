package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OModerationCase;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * data communication with the controllers `moderation_case`
 */
public class CModerationCase {


	public static OModerationCase findById(int caseId) {
		OModerationCase record = new OModerationCase();
		try (ResultSet rs = WebDb.get().select(
				"SELECT *  " +
						"FROM moderation_case " +
						"WHERE id = ? ", caseId)) {
			if (rs.next()) {
				record = fillRecord(rs);
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return record;
	}

	private static OModerationCase fillRecord(ResultSet resultset) throws SQLException {
		OModerationCase record = new OModerationCase();
		record.id = resultset.getInt("id");
		record.guildId = resultset.getInt("guild_id");
		record.userId = resultset.getInt("user_id");
		record.moderatorId = resultset.getInt("moderator");
		record.active = resultset.getInt("active");
		record.messageId = resultset.getLong("message");
		record.reason = resultset.getString("reason");
		record.createdAt = resultset.getTimestamp("created_at");
		record.expires = resultset.getTimestamp("expires");
		record.setPunishment(resultset.getInt("punishment"));
		return record;
	}

	public static void insert(OModerationCase record) {
		try {
			WebDb.get().insert(
					"INSERT INTO moderation_case(guild_id, user_id, moderator, message_id, created_at, reason, punishment, expires, active) " +
							"VALUES (?,?,?,?,?,?,?,?,?)",
					record.guildId, record.userId, record.moderatorId, record.messageId, record.createdAt, record.reason, record.punishment, record.expires, record.active);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void update(OModerationCase record) {
		try {
			WebDb.get().insert(
					"UPDATE moderation_case SET guild_id = ?, user_id = ?, " +
							"moderator = ?, message_id = ?, created_at = ?, reason = ?, punishment = ?, " +
							"expires =?, active = ? " +
							"WHERE id = ?" +
							record.guildId, record.userId,
					record.moderatorId, record.messageId, record.createdAt, record.reason, record.punishment,
					record.expires, record.active, record.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MessageEmbed buildCase(int caseId) {
		return buildCase(findById(caseId));
	}

	public static MessageEmbed buildCase(OModerationCase modcase) {
		EmbedBuilder b = new EmbedBuilder();
		b.setTitle(String.format("case #%s", modcase.id));
		b.setColor(modcase.punishment.getColor());
		b.addField("User", "" + modcase.userId, true);
		b.addField("Moderator", "" + modcase.moderatorId, true);
		b.addField("Issued", modcase.createdAt.toString(), true);
		b.addField("Punishment", modcase.punishment.getKeyword(), true);
		b.addField("Expires", modcase.expires.toString(), true);
		b.addField("Reason", modcase.reason, false);


		return b.build();
	}
}
