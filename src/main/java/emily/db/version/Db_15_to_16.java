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
 * bot events, add a log level to it
 */
public class Db_15_to_16 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 15;
    }

    @Override
    public int getToVersion() {
        return 16;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "ALTER TABLE bot_events ADD log_level INT DEFAULT 6 NULL",
                "ALTER TABLE bot_events MODIFY log_level INT(11) NOT NULL DEFAULT '6'"
        };
    }
}