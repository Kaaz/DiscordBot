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
import emily.db.model.OTodoList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CTodoLists {

    public static OTodoList findBy(int userId) {
        OTodoList t = new OTodoList();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM todo_list " +
                        "WHERE user_id = ? ", userId)) {
            if (rs.next()) {
                t = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return t;
    }

    private static OTodoList fillRecord(ResultSet rs) throws SQLException {
        OTodoList t = new OTodoList();
        t.id = rs.getInt("id");
        t.userId = rs.getInt("user_id");
        t.guildId = rs.getInt("guild_id");
        t.listName = rs.getString("list_name");
        t.visibility = rs.getInt("visibility");
        return t;
    }

    public static void delete(OTodoList record) {
        try {
            WebDb.get().query(
                    "DELETE FROM todo_list WHERE id = ? ",
                    record.id
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(OTodoList record) {
        if (record.id == 0) {
            insert(record);
            return;
        }
        try {
            record.id = WebDb.get().query(
                    "UPDATE todo_list SET user_id = ?, guild_id = ?, list_name = ?, visibility = ? WHERE id = ?",
                    record.userId, record.guildId, record.listName, record.visibility, record.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(OTodoList record) {
        if (record.id > 0) {
            update(record);
            return;
        }
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO todo_list(user_id, guild_id, list_name, visibility) " +
                            "VALUES (?,?,?,?)",
                    record.userId, record.guildId, record.listName, record.visibility);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
