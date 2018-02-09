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

package emily.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbUpdate {
    private final MySQLAdapter adapter;
    private final Pattern filepattern = Pattern.compile("(\\d+)_(\\d+).*\\.sql");
    private int highestVersion = 0;
    private Map<Integer, DbVersion> versionMap;

    public DbUpdate(MySQLAdapter adapter) throws IOException {
        this.adapter = adapter;
        versionMap = new HashMap<>();
        collectDatabaseVersions();
    }

    private void collectDatabaseVersions() throws IOException {
        final String path = "db_updates";
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        if (jarFile.isFile()) {  // Run with JAR file
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry file = entries.nextElement();
                if (file.getName().startsWith(path + "/")) {
                    prepareFile(file.getName());
                }
            }
            jar.close();
        } else {
            final URL url = getClass().getResource("/" + path);
            if (url != null) {
                try {
                    final File apps = new File(url.toURI());
                    File[] files = apps.listFiles();
                    if (files == null) {
                        return;
                    }
                    for (File file : files) {
                        prepareFile(path + "/" + file.getName());
                    }
                } catch (URISyntaxException ignored) {
                }
            }
        }
    }

    private void prepareFile(String filePath) {
        Matcher m = filepattern.matcher(filePath);
        if (!m.find()) {
            return;
        }
        int fromVersion = Integer.parseInt(m.group(1));
        int toVersion = Integer.parseInt(m.group(2));
        versionMap.put(fromVersion, new DbVersion(toVersion, filePath));
        highestVersion = Math.max(highestVersion, toVersion);
    }

    public boolean updateToCurrent() throws SQLException {
        int currentVersion = 0;
        try {
            currentVersion = getCurrentVersion();
        } catch (SQLException ignored) {
        }
        if (currentVersion == highestVersion) {
            return true;
        }
        SQLFileRunner runner = new SQLFileRunner(WebDb.get().getConnection(), true, true);
        boolean hasUpgrade = versionMap.containsKey(currentVersion);
        while (hasUpgrade) {
            DbVersion version = versionMap.get(currentVersion);
            System.out.println(version.file);
            try (InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(version.file));
                 BufferedReader br = new BufferedReader(reader)) {
                runner.runScript(br);
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentVersion = version.toVersion;
            saveDbVersion(currentVersion);
            hasUpgrade = versionMap.containsKey(currentVersion);
        }
        return true;
    }

//    public boolean updateToCurrent() {
//        int currentVersion = -1;
//        try {
//            currentVersion = getCurrentVersion();
//            if (currentVersion == highestVersion) {
//                return true;
//            }
//            boolean hasUpgrade = versionMap.containsKey(currentVersion);
//            while (hasUpgrade) {
//                IDbVersion dbVersion = versionMap.get(currentVersion);
//                for (String query : dbVersion.getExecutes()) {
//                    System.out.println("EXECUTING::");
//                    System.out.println(query);
//                    adapter.insert(query);
//                }
//                currentVersion = dbVersion.getToVersion();
//                saveDbVersion(currentVersion);
//                hasUpgrade = versionMap.containsKey(currentVersion);
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Db version: " + currentVersion);
//            e.printStackTrace();
//        }
//        return false;
//    }

    private int getCurrentVersion() throws SQLException {
        DatabaseMetaData metaData = adapter.getConnection().getMetaData();
        int dbVersion = 0;
        try (ResultSet rs = metaData.getTables(null, null, "commands", null)) {
            if (!rs.next()) {
                return 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try (ResultSet rs = metaData.getTables(null, null, "bot_meta", null)) {
            if (!rs.next()) {
                return dbVersion;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try (ResultSet rs = adapter.select("SELECT * FROM bot_meta WHERE meta_name = ?", "db_version")) {
            if (rs.next()) {
                dbVersion = Integer.parseInt(rs.getString("meta_value"));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return dbVersion;
    }

    private void saveDbVersion(int version) throws SQLException {
        if (version < 1) {
            return;
        }
        adapter.insert("INSERT INTO bot_meta(meta_name, meta_value) VALUES (?,?) ON DUPLICATE KEY UPDATE meta_value = ? ", "db_version", version, version);
    }

    private class DbVersion {
        final int toVersion;
        final String file;

        private DbVersion(int toVersion, String filePath) {
            this.toVersion = toVersion;
            this.file = filePath;
        }
    }
}