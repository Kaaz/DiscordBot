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
 * Start with a meta controllers for meta information such as database version
 */
public class db_00_to_01 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 0;
    }

    @Override
    public int getToVersion() {
        return 1;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "CREATE TABLE bot_meta (meta_name VARCHAR(32) PRIMARY KEY NOT NULL,  meta_value VARCHAR(32));"
        };
    }
}