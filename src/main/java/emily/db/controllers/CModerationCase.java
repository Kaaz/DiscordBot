/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.db.controllers;

import emily.core.Logger;
import emily.db.WebDb;
import emily.db.model.OModerationCase;
import emily.util.DisUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * data communication with the controllers `moderation_case`
 */
public class CModerationCase {

    private static final SimpleDateFormat banDateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

    /**
     * Finds the last case for a moderator within a guild
     *
     * @param guildId     internal guild id
     * @param moderatorId internal user id
     * @return case
     */
    public static OModerationCase findLastFor(int guildId, int moderatorId) {
        OModerationCase record = new OModerationCase();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM moderation_case " +
                        "WHERE guild_id = ? AND moderator = ? ORDER BY id DESC LIMIT 1", guildId, moderatorId)) {
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
        record.userName = resultset.getString("user_name");
        record.moderatorId = resultset.getInt("moderator");
        record.moderatorName = resultset.getString("moderator_name");
        record.active = resultset.getInt("active");
        record.messageId = resultset.getString("message_id");
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
        rec.userName = targetUser.getName() + "\\#" + targetUser.getDiscriminator();
        rec.moderatorId = CUser.getCachedId(moderator.getId());
        rec.moderatorName = moderator.getName() + "\\#" + moderator.getDiscriminator();
        rec.punishment = punishType;
        rec.expires = expires;
        rec.createdAt = new Timestamp(System.currentTimeMillis());
        rec.active = 1;
        rec.messageId = "1";
        return insert(rec);
    }

    public static int insert(OModerationCase record) {
        try {
            return WebDb.get().insert(
                    "INSERT INTO moderation_case(guild_id, user_id,user_name, moderator,moderator_name, message_id, created_at, reason, punishment, expires, active) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    record.guildId, record.userId, record.userName, record.moderatorId, record.moderatorName, record.messageId, record.createdAt, record.reason, record.punishment.getId(), record.expires, record.active);
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
                            "WHERE id = ?",
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
        b.setTitle(String.format("%s | case #%s", modcase.punishment.getKeyword(), modcase.id),null);
        b.setColor(modcase.punishment.getColor());
        b.addField("User", modcase.userName + "\n" + CUser.getCachedDiscordId(modcase.userId) + "\n", true);
        b.addField("Moderator", modcase.moderatorName + "\n" + CUser.getCachedDiscordId(modcase.moderatorId), true);
        b.addField("Issued", banDateFormat.format(modcase.createdAt), true);
        if (modcase.expires != null) {
            b.addField("Expires", modcase.expires.toString(), true);
        }
        String reason = modcase.reason;
        if (reason == null || reason.isEmpty()) {
            reason = "Reason not set! use `" + DisUtil.getCommandPrefix(guild) + "case reason " + modcase.id + " <message>` to set the reason";
        }
        b.addField("Reason", reason, false);


        return b.build();
    }
}
