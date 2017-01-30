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
 * init self assignable roles
 */
public class db_19_to_20 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 19;
    }

    @Override
    public int getToVersion() {
        return 20;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE guild_roles_self ( " +
                        " guild_id INT NOT NULL, " +
                        " discord_role_id VARCHAR(32)) ",
                "ALTER TABLE guild_roles_self ADD description TEXT NULL",
                "ALTER TABLE guild_roles_self ADD role_name VARCHAR(128) NULL",
                "ALTER TABLE guild_roles_self ADD PRIMARY KEY (guild_id, discord_role_id)"
        };
    }
}