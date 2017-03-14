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
 * Start the logging of bot events such as joining / leaving guilds
 */
public class Db_07_to_08 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 7;
    }

    @Override
    public int getToVersion() {
        return 8;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE bot_events ( " +
                        " id INT PRIMARY KEY AUTO_INCREMENT, " +
                        " created_on TIMESTAMP NOT NULL," +
                        " event_group VARCHAR(32) NOT NULL," +
                        " sub_group VARCHAR(32)," +
                        " data TEXT )"
        };
    }
}