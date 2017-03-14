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

package emily.db.version;

import emily.db.IDbVersion;

/**
 * more work on economy + introduction of betting
 * more indexes on tags
 */
public class Db_28_to_29 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 28;
    }

    @Override
    public int getToVersion() {
        return 29;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE INDEX tags_tag_name_guild_id_index ON tags (tag_name, guild_id)",
                "CREATE INDEX guild_name ON tags (tag_name)",
                "CREATE TABLE bets ( " +
                        " id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " title VARCHAR(128), " +
                        " owner_id INT NOT NULL, " +
                        " guild_id INT NOT NULL, " +
                        " created_on DATETIME NOT NULL, " +
                        " started_on DATETIME, " +
                        " ends_at DATETIME, " +
                        " price INT )",
                "CREATE TABLE bet_options ( " +
                        " id INT NOT NULL AUTO_INCREMENT, " +
                        " bet_id INT NOT NULL, " +
                        " description VARCHAR(128), " +
                        " CONSTRAINT bet_options_id_bet_id_pk PRIMARY KEY (id, bet_id) )",
                "ALTER TABLE bets ADD bet_status INT DEFAULT 0 NOT NULL",
                "CREATE TABLE todo_list ( " +
                        "id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, " +
                        "user_id INT NOT NULL, " +
                        "guild_id INT NOT NULL, " +
                        "list_name VARCHAR(191) NOT NULL, " +
                        "visibility INT " +
                        ")",
                "CREATE INDEX todo_list_guild_id_index ON todo_list (guild_id)",
                "CREATE INDEX todo_list_user_id_index ON todo_list (user_id)",
                "CREATE INDEX todo_list_guild_id_user_id_index ON todo_list (guild_id, user_id)",
                "CREATE TABLE todo_item( " +
                        "id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT, " +
                        "list_id INT(11) NOT NULL, " +
                        "description VARCHAR(191), " +
                        "checked INT(11), " +
                        "priority INT(11)) "

        };
    }
}