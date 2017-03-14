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
 * introduction of music_log; keep track of whats being played
 */
public class Db_11_to_12 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 11;
    }

    @Override
    public int getToVersion() {
        return 12;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE music_log(\n" +
                        " id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,\n" +
                        " music_id INT(11) DEFAULT '0' NOT NULL,\n" +
                        " guild_id INT(11) DEFAULT '0' NOT NULL,\n" +
                        " user_id INT(11),\n" +
                        " play_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL )\n",
                "CREATE INDEX music_log_guild_id_index ON music_log (guild_id)",
                "CREATE INDEX music_log_guild_id_music_id_index ON music_log (guild_id, music_id)",
                "CREATE INDEX music_log_music_id_index ON music_log (music_id)",
                "CREATE INDEX music_filename_unique_index ON music (filename)",
                "CREATE INDEX music_youtubecode_unique_index ON music (youtubecode)",
        };
    }
}