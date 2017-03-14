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

package emily.db.controllers;

import emily.core.Logger;
import emily.db.WebDb;
import emily.db.model.OBotVersion;
import emily.main.ProgramVersion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the controllers `bot_versions`
 */
public class CBotVersions {

    public static OBotVersion findBy(ProgramVersion version) {
        OBotVersion s = new OBotVersion();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM bot_versions " +
                        "WHERE major= ? AND minor = ? AND patch = ? ", version.getMajorVersion(), version.getMinorVersion(), version.getPatchVersion())) {
            if (rs.next()) {
                s = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    /**
     * Retrieves the version after after specified one
     *
     * @param version bot version
     * @return version || null
     */
    public static OBotVersion versionBefore(ProgramVersion version) {
        OBotVersion s = new OBotVersion();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM bot_versions " +
                        " " +
                        "WHERE major < ? " +
                        "OR (major = ? AND minor < ?) " +
                        "OR (major = ? AND minor = ? AND patch < ?) " +
                        "ORDER BY major DESC , minor DESC , patch DESC ",
                version.getMajorVersion(),
                version.getMajorVersion(), version.getMinorVersion(),
                version.getMajorVersion(), version.getMinorVersion(), version.getPatchVersion())) {
            if (rs.next()) {
                s = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    /**
     * Retrieves the version before specified one
     *
     * @param version bot version
     * @return version || null
     */
    public static OBotVersion versionAfter(ProgramVersion version) {
        OBotVersion s = new OBotVersion();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * FROM bot_versions " +
                        " " +
                        "WHERE major > ? " +
                        "OR (major = ? AND minor > ?) " +
                        "OR (major = ? AND minor = ? AND patch > ?) " +
                        "ORDER BY major ASC , minor ASC , patch ASC ",
                version.getMajorVersion(),
                version.getMajorVersion(), version.getMinorVersion(),
                version.getMajorVersion(), version.getMinorVersion(), version.getPatchVersion())) {
            if (rs.next()) {
                s = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return s;
    }

    private static OBotVersion fillRecord(ResultSet rs) throws SQLException {
        OBotVersion s = new OBotVersion();
        s.id = rs.getInt("id");
        s.major = rs.getInt("major");
        s.minor = rs.getInt("minor");
        s.patch = rs.getInt("patch");
        s.createdOn = rs.getTimestamp("created_on");
        s.published = rs.getInt("published");
        return s;
    }

    public static void publish(ProgramVersion version, boolean publish) {
        try {
            WebDb.get().insert(
                    "UPDATE bot_versions SET published = ? " +
                            " WHERE major = ? AND minor = ? AND patch = ?",
                    publish ? 1 : 0, version.getMajorVersion(), version.getMinorVersion(), version.getPatchVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static int insert(ProgramVersion version, Timestamp date) {
        try {
            if (date == null) {
                date = new Timestamp(System.currentTimeMillis());
            }
            return WebDb.get().insert(
                    "INSERT INTO bot_versions(major, minor, patch, created_on, published) " +
                            "VALUES (?,?,?,?,?)",
                    version.getMajorVersion(), version.getMinorVersion(), version.getPatchVersion(), date, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}