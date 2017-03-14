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
 * guild moderation cases
 */
public class Db_23_to_24 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 23;
    }

    @Override
    public int getToVersion() {
        return 24;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE moderation_case( " +
                        " id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                        " guild_id INT(11) NOT NULL," +
                        " user_id INT(11) NOT NULL," +
                        " moderator INT(11)," +
                        " message_id INT(21) NOT NULL," +
                        " created_at DATETIME NOT NULL," +
                        " reason TEXT NOT NULL," +
                        " punishment INT(11) NOT NULL," +
                        " expires DATETIME," +
                        " active INT(11) NOT NULL )",
                "CREATE UNIQUE INDEX moderation_case_guild_id_user_id_pk ON moderation_case (guild_id, user_id)",
                "CREATE UNIQUE INDEX moderation_case_guild_id_message_id_pk ON moderation_case (guild_id, message_id)",
                "CREATE UNIQUE INDEX moderation_case_user_id_message_id_pk ON moderation_case (user_id, message_id)",
        };
    }
}