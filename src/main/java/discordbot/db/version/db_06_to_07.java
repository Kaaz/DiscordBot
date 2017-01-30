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
 * Began on simple user ranking, save the unusual users/ranks in the database
 */
public class db_06_to_07 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 6;
    }

    @Override
    public int getToVersion() {
        return 7;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE user_rank ( user_id INT, rank_type INT )",
                "ALTER TABLE user_rank ADD PRIMARY KEY (rank_type, user_id)",
                "CREATE TABLE ranks ( id INT PRIMARY KEY AUTO_INCREMENT, code_name VARCHAR(32) NOT NULL, full_name VARCHAR(255) )",
                "CREATE UNIQUE INDEX ranks_code_name_uindex ON ranks (code_name)",
                "INSERT INTO ranks (code_name, full_name) VALUES ('BOT_ADMIN', 'Bot Administrator')",
                "INSERT INTO ranks (code_name, full_name) VALUES ('CONTRIBUTOR', 'Contributor')",
        };
    }
}