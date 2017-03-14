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

package emily.service;

import emily.core.AbstractService;
import emily.db.WebDb;
import emily.db.controllers.CMusic;
import emily.db.model.OMusic;
import emily.main.BotContainer;
import emily.main.Launcher;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * cleans up unused cached music files
 */
public class MusicCleanupService extends AbstractService {

    public MusicCleanupService(BotContainer b) {
        super(b);
    }

    @Override
    public String getIdentifier() {
        return "music_cleanup_service";
    }

    @Override
    public long getDelayBetweenRuns() {
        return TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public boolean shouldIRun() {
        return true;
    }

    @Override
    public void beforeRun() {
    }

    @Override
    public void run() {
        long olderThan = (System.currentTimeMillis() / 1000L) - TimeUnit.DAYS.toSeconds(7);
        try (ResultSet rs = WebDb.get().select("SELECT m.* " +
                " FROM music m " +
                " WHERE m.lastplaydate < ? " +
                " AND m.file_exists = 1 " +
                " ORDER BY lastplaydate DESC", olderThan)) {
            while (rs.next()) {
                OMusic record = CMusic.fillRecord(rs);
                File file = new File(record.filename);
                if (file.exists()) {
                    file.delete();
                }
                record.fileExists = 0;
                CMusic.update(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Launcher.logToDiscord(e);
        }
    }

    @Override
    public void afterRun() {
    }
}