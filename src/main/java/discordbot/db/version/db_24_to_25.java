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
 * guild mod case
 */
public class db_24_to_25 implements IDbVersion {
    @Override
    public int getFromVersion() {
        return 24;
    }

    @Override
    public int getToVersion() {
        return 25;
    }

    @Override
    public String[] getExecutes() {
        return new String[]{
                "DROP INDEX moderation_case_guild_id_user_id_pk ON moderation_case",
                "DROP INDEX moderation_case_guild_id_message_id_pk ON moderation_case",
                "CREATE INDEX moderation_case_guild_id_message_id_pk ON moderation_case(guild_id, message_id)",
                "DROP INDEX moderation_case_user_id_message_id_pk ON moderation_case",
                "CREATE INDEX moderation_case_user_id_message_id_pk ON moderation_case(user_id, message_id)",
        };
    }
}