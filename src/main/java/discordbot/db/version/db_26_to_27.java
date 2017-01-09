package discordbot.db.version;

import discordbot.db.IDbVersion;

/**
 * some progress on the economy part
 */
public class db_26_to_27 implements IDbVersion {
	@Override
	public int getFromVersion() {
		return 26;
	}

	@Override
	public int getToVersion() {
		return 27;
	}

	@Override
	public String[] getExecutes() {
		return new String[]{
				"ALTER TABLE users ADD last_currency_retrieval INT DEFAULT 0 NOT NULL",
				"ALTER TABLE bank_transactions ADD amount INT NOT NULL",
				"CREATE INDEX bank_transactions_bank_from_index ON bank_transactions (bank_from)",
				"CREATE INDEX bank_transactions_bank_to_index ON bank_transactions (bank_to)",
				"CREATE INDEX bank_transactions_bank_from_bank_to_index ON bank_transactions (bank_from, bank_to)"
		};
	}
}