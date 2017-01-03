package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OModerationCase;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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
		record.messageId = resultset.getLong("message_id");
		record.reason = resultset.getString("reason");
		record.createdAt = resultset.getTimestamp("created_at");
		record.expires = resultset.getTimestamp("expires");
		record.setPunishment(resultset.getInt("punishment"));
		return record;
	}

	public static int insert(Guild guild, User targetUser, User moderator, OModerationCase.PunishType punishType, Timestamp expires) {
		OModerationCase rec = new OModerationCase();
		rec.guildId = CGuild.getCachedId(guild.getId());
		rec.userId = CUser.getCachedId(targetUser.getId());
		rec.moderatorId = CUser.getCachedId(moderator.getId());
		rec.punishment = punishType;
		rec.expires = expires;
		rec.createdAt = new Timestamp(System.currentTimeMillis());
		rec.active = 1;
		rec.messageId = 1;
		return insert(rec);
	}

	public static int insert(OModerationCase record) {
		try {
			return WebDb.get().insert(
					"INSERT INTO moderation_case(guild_id, user_id, moderator, message_id, created_at, reason, punishment, expires, active) " +
							"VALUES (?,?,?,?,?,?,?,?,?)",
					record.guildId, record.userId, record.moderatorId, record.messageId, record.createdAt, record.reason, record.punishment.getId(), record.expires, record.active);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static void update(OModerationCase record) {
		try {
			WebDb.get().insert(
					"UPDATE moderation_case SET guild_id = ?, user_id = ?, " +
							"moderator = ?, message_id = ?, created_at = ?, reason = ?, punishment = ?, " +
							"expires =?, active = ? " +
							"WHERE id = ?" +
							record.guildId, record.userId,
					record.moderatorId, record.messageId, record.createdAt, record.reason, record.punishment.getId(),
					record.expires, record.active, record.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MessageEmbed buildCase(Guild guild, int caseId) {
		return buildCase(guild, findById(caseId));
	}

	public static MessageEmbed buildCase(Guild guild, OModerationCase modcase) {
		EmbedBuilder b = new EmbedBuilder();
		b.setTitle(String.format("%s | case #%s", modcase.punishment.getKeyword(), modcase.id));
		b.setColor(modcase.punishment.getColor());
		b.addField("User", "" + CUser.getCachedDiscordId(modcase.userId), true);
		b.addField("Moderator", "" + CUser.getCachedDiscordId(modcase.moderatorId), true);
		b.addField("Issued", modcase.createdAt.toString(), true);
		if (modcase.expires != null) {
			b.addField("Expires", modcase.expires.toString(), true);
		}
		String reason = modcase.reason;
		if (reason == null || reason.isEmpty()) {
			reason = "Reason not set! use `" + DisUtil.getCommandPrefix(guild) + "case " + modcase.id + "` to set the reason";
		}
		b.addField("Reason", reason, false);


		return b.build();
	}
}
