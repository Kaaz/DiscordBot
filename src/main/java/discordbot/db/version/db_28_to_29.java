package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * more work on economy + introduction of betting
 */
public class db_28_to_29 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 28;
	}

	@Override
	public int getToVersion() {
		return 28;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE bets ( " +
						" id INT PRIMARY KEY AUTO_INCREMENT, " +
						" title VARCHAR(128), " +
						" owner_id INT NOT NULL, " +
						" guild_id INT NOT NULL, " +
						" created_on DATETIME NOT NULL, " +
						" started_on DATETIME, " +
						" ends_at DATETIME, " +
						" price INT )",
				"CREATE TABLE bet_options ( " +
						" id INT NOT NULL AUTO_INCREMENT, " +
						" bet_id INT NOT NULL, " +
						" description VARCHAR(128), " +
						" CONSTRAINT bet_options_id_bet_id_pk PRIMARY KEY (id, bet_id) )",
				"ALTER TABLE bets ADD bet_status INT DEFAULT 0 NOT NULL"
		};
	}
}