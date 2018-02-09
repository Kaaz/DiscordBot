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

package emily.db.model;

import emily.db.AbstractModel;

import java.awt.*;
import java.sql.Timestamp;

public class OModerationCase extends AbstractModel {
    public int guildId = 0;
    public int userId = 0;
    public int id = 0;
    public int moderatorId = 0;
    public String messageId = "";
    public Timestamp createdAt = null;
    public Timestamp expires = null;
    public PunishType punishment = PunishType.KICK;
    public String reason = "";
    public int active = 1;
    public String moderatorName = "";
    public String userName = "";

    public void setPunishment(int punishment) {
        this.punishment = PunishType.fromId(punishment);
    }

    public enum PunishType {
        WARN(1, "Warn", "warned", "Adds a strike to the user", new Color(0xA8CF00)),
        MUTE(2, "Mute", "muted", "Adds the configured muted role to user", new Color(0xFFF300)),
        KICK(3, "Kick", "kicked", "Remove user from the guild", new Color(0xFF9600)),
        TMP_BAN(4, "Temp-ban", "temporarily banned", "Remove user from guild, unable to rejoin for a while", new Color(0xFF4700)),
        BAN(5, "Ban", "banned", "Permanently removes user from guild", new Color(0xB70000));

        private final int id;
        private final String keyword;
        private final String verb;
        private final String description;
        private final Color color;

        PunishType(int id, String keyword, String verb, String description, Color color) {
            this.id = id;
            this.keyword = keyword;
            this.description = description;
            this.color = color;
            this.verb = verb;
        }

        public static PunishType fromId(int id) {
            for (PunishType et : values()) {
                if (id == et.getId()) {
                    return et;
                }
            }
            return KICK;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getKeyword() {
            return keyword;
        }

        public Color getColor() {
            return color;
        }

        public String getVerb() {
            return verb;
        }
    }
}
