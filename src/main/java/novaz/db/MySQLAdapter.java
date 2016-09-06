package novaz.db;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import novaz.exceptions.UnimplementedParameterException;

import java.sql.*;
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
			dataSource.setCharacterEncoding("utf-8");
			return dataSource.getConnection();
		} catch (SQLException e) {
			System.out.println("Can't connect to the database! Make sure the database settings are corrent and the database server is running");
		}
		return null;
	}

	private Connection getConnection() {
		if (c == null) {
			c = createConnection();
		}
		return c;
	}

	public ResultSet select(String sql, Object... params) {
		PreparedStatement query = null;
		try {
			query = getConnection().prepareStatement(sql);
			resolveParameters(query, params);
			return query.executeQuery();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public int query(String sql) throws SQLException {
		try (Statement stmt = getConnection().createStatement();) {
			int res = stmt.executeUpdate(sql);
			return res;
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
			return query.executeUpdate();
		}
	}
}
