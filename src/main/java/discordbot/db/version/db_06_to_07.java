package discordbot.db.version;

import discordbot.db.IDbVersion;

public class db_06_to_07 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 6;
	}

	@Override
	public int getToVersion() {
		return 7;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"CREATE TABLE user_rank ( user_id INT, rank_type INT )",
				"ALTER TABLE user_rank ADD PRIMARY KEY (rank_type, user_id)",
				"CREATE TABLE ranks ( id INT PRIMARY KEY AUTO_INCREMENT, code_name VARCHAR(32) NOT NULL, full_name VARCHAR(255) )",
				"CREATE UNIQUE INDEX ranks_code_name_uindex ON discord.ranks (code_name)",
				"INSERT INTO ranks (code_name, full_name) VALUES ('BOT_ADMIN', 'Bot Administrator')",
				"INSERT INTO ranks (code_name, full_name) VALUES ('CONTRIBUTOR', 'Contributor')",
		};
	}
}