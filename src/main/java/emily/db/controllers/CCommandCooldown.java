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
import emily.db.model.OCommandCooldown;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * data communication with the controllers `service_variables`
 */
public class CCommandCooldown {

    public static OCommandCooldown findBy(String commandName, String targetId, int targetType) {
        OCommandCooldown record = new OCommandCooldown();
        try (ResultSet rs = WebDb.get().select(
                "SELECT command, target_id, target_type, last_time  " +
                        "FROM command_cooldown " +
                        "WHERE command = ? AND target_id = ? AND target_type = ?", commandName, targetId, targetType)) {
            if (rs.next()) {
                record = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return record;
    }

    private static OCommandCooldown fillRecord(ResultSet resultset) throws SQLException {
        OCommandCooldown record = new OCommandCooldown();
        record.command = resultset.getString("command");
        record.targetId = resultset.getString("target_id");
        record.targetType = resultset.getInt("target_type");
        record.lastTime = resultset.getLong("last_time");
        return record;
    }

    public static void insertOrUpdate(OCommandCooldown record) {
        try {
            WebDb.get().insert(
                    "INSERT INTO command_cooldown(command, target_id, target_type, last_time) " +
                            "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE last_time = ?",
                    record.command, record.targetId, record.targetType, record.lastTime, record.lastTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
