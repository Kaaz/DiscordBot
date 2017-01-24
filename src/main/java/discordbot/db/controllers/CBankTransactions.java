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

package discordbot.db.controllers;

import discordbot.core.Logger;
import discordbot.db.WebDb;
import discordbot.db.model.OBankTransaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
		bank.userFrom = resultset.getString("user_from");
		bank.userTo = resultset.getString("user_to");
		return bank;
	}

	public static List<OBankTransaction> getHistoryFor(int bankId) {
		List<OBankTransaction> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT t.*, uf.name AS user_from, ut.name AS user_to " +
						"FROM bank_transactions t " +
						"JOIN banks bf ON bf.id = t.bank_from " +
						"JOIN users uf ON uf.id = bf.user " +
						"JOIN banks bt ON bt.id = t.bank_to " +
						"JOIN users ut ON ut.id = bt.user " +
						"WHERE t.bank_from  = ?  OR t.bank_to = ? ORDER BY t.id DESC LIMIT 25", bankId, bankId)) {
			while (rs.next()) {
				ret.add(fillRecord(rs));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			Logger.fatal(e);
		}
		return ret;
	}

	public static void insert(int bankFrom, int bankTo, int amount, String description) {
		OBankTransaction rec = new OBankTransaction();
		rec.bankFrom = bankFrom;
		rec.bankTo = bankTo;
		rec.amount = amount;
		rec.description = description;
		insert(rec);
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
