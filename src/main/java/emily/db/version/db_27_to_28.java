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

//message saved as varchar
public class db_27_to_28 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 27;
    }

    @Override
    public int getToVersion() {
        return 28;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "ALTER TABLE moderation_case MODIFY message_id VARCHAR(32)",
                "ALTER TABLE moderation_case ADD user_name VARCHAR(64) NULL",
                "ALTER TABLE moderation_case ADD moderator_name VARCHAR(64) NULL",
                "TRUNCATE TABLE moderation_case"
        };
    }
}