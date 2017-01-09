package discordbot.db.model;

import discordbot.db.AbstractModel;
import discordbot.db.controllers.CBankTransactions;
import discordbot.db.controllers.CBanks;

import java.sql.Timestamp;

/**
 * Created on 5-9-2016
 */
public class OBank extends AbstractModel {
	public int userId = 0;
	public int id = 0;
	public long currentBalance = 0L;
	public Timestamp createdOn = null;

	public boolean transferTo(OBank target, int amount, String description) {
		if (id == 0) {
			return false;
		}
		if (amount < 1 || currentBalance - amount < 0) {
			return false;
		}
		if (description != null && description.length() > 150) {
			description = description.substring(0, 150);
		}
		CBankTransactions.insert(id, target.id, amount, description);
		target.currentBalance += amount;
		currentBalance -= amount;
		CBanks.updateBalance(id, -amount);
		CBanks.updateBalance(target.id, amount);
		return true;
	}
}
