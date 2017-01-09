package discordbot.db.controllers;

import discordbot.db.WebDb;
import discordbot.db.model.OBankTransaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the controllers `bank_transactions`
 */
public class CBankTransactions {

	private static OBankTransaction fillRecord(ResultSet resultset) throws SQLException {
		OBankTransaction bank = new OBankTransaction();
		bank.id = resultset.getInt("id");
		bank.bankFrom = resultset.getInt("bank_from");
		bank.bankTo = resultset.getInt("bank_to");
		bank.amount = resultset.getInt("amount");
		bank.date = resultset.getTimestamp("transaction_date");
		bank.description = resultset.getString("description");
		return bank;
	}

	public static void insert(int bankFrom, int bankTo, int amount, String description) {
		OBankTransaction rec = new OBankTransaction();
		rec.bankFrom = bankFrom;
		rec.bankTo = bankTo;
		rec.amount = amount;
		rec.description = description;
	}

	public static void insert(OBankTransaction rec) {
		try {
			if (rec.date == null) {
				rec.date = new Timestamp(System.currentTimeMillis());
			}
			rec.id = WebDb.get().insert(
					"INSERT INTO bank_transactions(bank_from, bank_to,transaction_date, amount,description) " +
							"VALUES (?,?,?,?,?)",
					rec.bankFrom, rec.bankTo, rec.date, rec.amount, rec.description);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
