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

package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * introduction of playlists
 */
public class db_09_to_10 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 9;
    }

    @Override
    public int getToVersion() {
        return 10;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE playlist( " +
                        " id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                        " title VARCHAR(255) NOT NULL," +
                        " owner_id INT(11) NOT NULL," +
                        " guild_id INT(11) NOT NULL," +
                        " visibility_level INT(11) NOT NULL," +
                        " edit_type INT(11) NOT NULL," +
                        " create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                        ");",
                " CREATE TABLE playlist_item (" +
                        " playlist_id INT(11)DEFAULT'0'NOT NULL," +
                        " music_id INT(11)DEFAULT'0'NOT NULL," +
                        " last_played INT(21)," +
                        " CONSTRAINT `PRIMARY`PRIMARY KEY (playlist_id, music_id)" +
                        " )",
        };
    }
}