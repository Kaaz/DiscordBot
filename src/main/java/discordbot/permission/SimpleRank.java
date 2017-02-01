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

package discordbot.permission;

/**
 *
 */
public enum SimpleRank {
    BANNED_USER(),
    BOT(),
    USER(),
    INTERACTION_BOT(),
    GUILD_ADMIN(),
    GUILD_OWNER(),
    CONTRIBUTOR(),
    BOT_ADMIN(),
    SYSTEM_ADMIN(),
    CREATOR();

    /**
     * find a rank by name
     *
     * @param search the role to search for
     * @return rank || null
     */
    public static SimpleRank findRank(String search) {
        for (SimpleRank simpleRank : values()) {
            if (simpleRank.name().equalsIgnoreCase(search)) {
                return simpleRank;
            }
        }
        return null;
    }

    public boolean isAtLeast(SimpleRank rank) {
        return this.ordinal() >= rank.ordinal();
    }

    public boolean isHigherThan(SimpleRank rank) {
        return this.ordinal() > rank.ordinal();
    }
}
