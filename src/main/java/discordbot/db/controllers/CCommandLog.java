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

package discordbot.db.controllers;

import discordbot.db.WebDb;

import java.sql.Date;

/**
 * data communication with the controllers `command_log`
 * Created on 30-8-2016
 */
public class CCommandLog {

    public static void saveLog(int userId, int guildId, String commandUsed, String commandArgs) {
        try {
            WebDb.get().insert(
                    "INSERT INTO command_log(user_id, guild, command, args, execute_date) " +
                            "VALUES (?,?,?,?,?)",
                    userId, guildId, commandUsed, commandArgs, new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}