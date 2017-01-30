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
 * playlist: + playmode
 */
public class db_14_to_15 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 14;
    }

    @Override
    public int getToVersion() {
        return 15;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "ALTER TABLE music ADD play_count INT NOT NULL",
                "ALTER TABLE music ADD last_manual_playdate INT NOT NULL",
                "ALTER TABLE users ADD commands_used INT NOT NULL",
        };
    }
}