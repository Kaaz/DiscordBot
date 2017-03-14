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
 * enabling/disabling commands per guild
 */
public class db_17_to_18 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 17;
    }

    @Override
    public int getToVersion() {
        return 18;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                " CREATE TABLE blacklist_commands ( " +
                        " guild_id INT NOT NULL, " +
                        " command VARCHAR(64) NOT NULL, " +
                        " CONSTRAINT blacklist_commands_guild_id_command_pk PRIMARY KEY (guild_id, command) " +
                        " )",
                "CREATE INDEX blacklist_commands_guild_id_index ON blacklist_commands (guild_id)"
        };
    }
}