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
 * saving some info on shutdown
 * Save the channels where the bot was playing on, to resume again on startup.
 */
public class Db_12_to_13 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 12;
    }

    @Override
    public int getToVersion() {
        return 13;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE bot_playing_on ( " +
                        "guild_id VARCHAR(32), " +
                        "channel_id VARCHAR(32), " +
                        "CONSTRAINT bot_playing_on_pk PRIMARY KEY (guild_id, channel_id))",

        };
    }
}