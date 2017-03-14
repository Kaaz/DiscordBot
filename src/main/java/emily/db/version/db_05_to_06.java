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
 * Renamed a couple of tables to match the discord names
 * added the option to ban users, guilds
 * added a few indices to make searching a bit better
 */
public class db_05_to_06 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 5;
    }

    @Override
    public int getToVersion() {
        return 6;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "ALTER TABLE servers ADD banned INT NULL",
                "ALTER TABLE servers RENAME TO guilds",
                "CREATE UNIQUE INDEX users_discord_id_uindex ON users (discord_id)",
                "ALTER TABLE playlist RENAME TO music",
                "ALTER TABLE users ADD banned INT NULL",
                "CREATE UNIQUE INDEX guilds_discord_id_uindex ON guilds (discord_id)"
        };
    }
}