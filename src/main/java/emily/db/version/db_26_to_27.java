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
 * some progress on the economy part
 * and tables for !changelog
 */
public class db_26_to_27 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 26;
    }

    @Override
    public int getToVersion() {
        return 27;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "ALTER TABLE users ADD last_currency_retrieval INT DEFAULT 0 NOT NULL",
                "ALTER TABLE bank_transactions ADD amount INT NOT NULL",
                "CREATE INDEX bank_transactions_bank_from_index ON bank_transactions (bank_from)",
                "CREATE INDEX bank_transactions_bank_to_index ON bank_transactions (bank_to)",
                "CREATE INDEX bank_transactions_bank_from_bank_to_index ON bank_transactions (bank_from, bank_to)",
                "CREATE TABLE bot_versions ( id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "major INT NOT NULL, " +
                        "minor INT NOT NULL, " +
                        "patch INT NOT NULL, " +
                        "created_on INT )",
                "CREATE UNIQUE INDEX bot_versions_major_minor_patch_uindex ON bot_versions (major, minor, patch)",
                "CREATE TABLE bot_version_changes(" +
                        " id INT PRIMARY KEY AUTO_INCREMENT," +
                        " version INT NOT NULL," +
                        " change_type INT," +
                        " description VARCHAR(128)," +
                        " author INT" +
                        ")",
                "CREATE INDEX bot_version_changes_version_index ON bot_version_changes (version)",
                "ALTER TABLE bot_versions MODIFY created_on TIMESTAMP",
                "ALTER TABLE bot_versions ADD published INT DEFAULT 0 NULL"
        };
    }
}