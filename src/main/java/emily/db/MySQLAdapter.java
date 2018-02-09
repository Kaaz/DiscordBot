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

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import emily.core.ExitCode;
import emily.exceptions.UnimplementedParameterException;
import emily.main.DiscordBot;
import emily.main.Launcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

public class MySQLAdapter {

    protected String DB_NAME;
    protected String DB_USER;
    protected String DB_ADRES;
    protected String DB_PASSWORD;
    private Connection c;

    public MySQLAdapter(String server, String databaseUser, String databasePassword, String databaseName) {
        DB_ADRES = server;
        DB_USER = databaseUser;
        DB_PASSWORD = databasePassword;
        DB_NAME = databaseName;
    }

    private Connection createConnection() {
        try {
            MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setUser(DB_USER);
            dataSource.setPassword(DB_PASSWORD);
            dataSource.setServerName(DB_ADRES);
            dataSource.setPort(3306);
            dataSource.setDatabaseName(DB_NAME);
            dataSource.setZeroDateTimeBehavior("convertToNull");
            dataSource.setUseUnicode(true);
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            DiscordBot.LOGGER.error("Can't connect to the database! Make sure the database settings are corrent and the database server is running AND the database `" + DB_NAME + "` exists");
            Launcher.stop(ExitCode.SHITTY_CONFIG);
        }
        return null;
    }

    public Connection getConnection() {
        if (c == null) {
            c = createConnection();
        }
        return c;
    }

    public ResultSet select(String sql, Object... params) throws SQLException {
        PreparedStatement query;
        query = getConnection().prepareStatement(sql);
        resolveParameters(query, params);
        return query.executeQuery();
    }

    public int query(String sql) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    private void resolveParameters(PreparedStatement query, Object... params) throws SQLException {
        int index = 1;
        for (Object p : params) {
            if (p instanceof String) {
                query.setString(index, (String) p);
            } else if (p instanceof Integer) {
                query.setInt(index, (int) p);
            } else if (p instanceof Long) {
                query.setLong(index, (Long) p);
            } else if (p instanceof Double) {
                query.setDouble(index, (double) p);
            } else if (p instanceof java.sql.Date) {
                java.sql.Date d = (java.sql.Date) p;
                Timestamp ts = new Timestamp(d.getTime());
                query.setTimestamp(index, ts);
            } else if (p instanceof java.util.Date) {
                java.util.Date d = (java.util.Date) p;
                Timestamp ts = new Timestamp(d.getTime());
                query.setTimestamp(index, ts);
            } else if (p instanceof Calendar) {
                Calendar cal = (Calendar) p;
                Timestamp ts = new Timestamp(cal.getTimeInMillis());
                query.setTimestamp(index, ts);
            } else if (p == null) {
                query.setNull(index, Types.NULL);
            } else {
                throw new UnimplementedParameterException(p, index);
            }
            index++;
        }
    }

    public int query(String sql, Object... params) throws SQLException {
        try (PreparedStatement query = getConnection().prepareStatement(sql)) {
            resolveParameters(query, params);
            return query.executeUpdate();
        }
    }

    public int insert(String sql, Object... params) throws SQLException {
        try (PreparedStatement query = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            resolveParameters(query, params);
            query.executeUpdate();
            ResultSet rs = query.getGeneratedKeys();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
}
