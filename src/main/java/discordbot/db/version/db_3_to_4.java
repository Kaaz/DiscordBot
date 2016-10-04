package discordbot.db.version;

import discordbot.db.IDbVersion;

public class db_3_to_4 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 3;
	}

	@Override
	public int getToVersion() {
		return 4;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE servers ADD active INT NULL"
		};
	}
}