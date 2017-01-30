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

package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.sql.Timestamp;

public class OBet extends AbstractModel {
    public int id = 0;
    public int guildId = 0;
    public String title = "";
    public int ownerId = 0;
    public Timestamp createdOn = null;
    public Timestamp startedOn = null;
    public Timestamp endsAt = null;
    public int price = 0;
    public Status status = Status.PREPARING;

    public void setStatus(int id) {
        status = Status.fromId(id);
    }

    public enum Status {
        PREPARING(1),
        PENDING(2),
        ACTIVE(3),
        CLOSED(4),
        CANCELED(5);

        private int id;

        Status(int id) {

            this.id = id;
        }

        public static Status fromId(int id) {
            for (Status s : values()) {
                if (id == s.getId()) {
                    return s;
                }
            }
            return PREPARING;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
