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
 * command blacklist for specific channels
 */
public class db_20_to_21 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 20;
    }

    @Override
    public int getToVersion() {
        return 21;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "ALTER TABLE blacklist_commands ADD channel_id VARCHAR(32)NOT NULL",
                "ALTER TABLE blacklist_commands DROP PRIMARY KEY",
                "ALTER TABLE blacklist_commands ADD PRIMARY KEY(guild_id, command, channel_id)",
                "ALTER TABLE blacklist_commands ADD enabled INT DEFAULT 0 NULL",
        };
    }
}