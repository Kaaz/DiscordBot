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

package emily.db.controllers;

import emily.core.Logger;
import emily.db.WebDb;
import emily.db.model.OBank;
import emily.db.model.OUser;
import emily.main.BotConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * data communication with the controllers `banks`
 */
public class CBanks {

    //the amount of currency you claim each hour
    public static final double CURRENCY_PER_HOUR = 0.5D;
    public static final long SECONDS_PER_CURRENCY = (long) (1 / CURRENCY_PER_HOUR * 3600D);
    //after reaching this amount amount, you can't claim anymore
    public static final long CURRENCY_NO_HELP_AFTER = 10000;
    //the max currency you can get from a claim
    public static int CURRENCY_GIVEAWAY_MAX = (int) (CURRENCY_PER_HOUR * 24D);
    private static volatile OBank BOT_BANK_ACCOUNT = null;

    public static OBank findBy(String discordId) {
        return findBy(CUser.getCachedId(discordId));
    }

    public static OBank getBotAccount() {
        return BOT_BANK_ACCOUNT;
    }

    public static OBank findBy(int userId) {
        OBank bank = new OBank();
        try (ResultSet rs = WebDb.get().select(
                "SELECT id, user, current_balance, created_on  " +
                        "FROM banks " +
                        "WHERE user = ? ", userId)) {
            if (rs.next()) {
                bank = fillRecord(rs);
            } else {
                bank.userId = userId;
                insert(bank);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            Logger.fatal(e);
        }
        return bank;
    }

    private static OBank fillRecord(ResultSet resultset) throws SQLException {
        OBank bank = new OBank();
        bank.id = resultset.getInt("id");
        bank.userId = resultset.getInt("user");
        bank.currentBalance = resultset.getLong("current_balance");
        bank.createdOn = resultset.getTimestamp("created_on");
        return bank;
    }

    public static void insert(OBank bank) {
        if (bank.id > 0) {
            update(bank);
            return;
        }
        try {
            if (bank.currentBalance == 0L) {
                bank.currentBalance = BotConfig.ECONOMY_START_BALANCE;
            }
            bank.createdOn = new Timestamp(System.currentTimeMillis());
            bank.id = WebDb.get().insert(
                    "INSERT INTO banks(user, current_balance, created_on) " +
                            "VALUES (?,?,?)",
                    bank.userId, bank.currentBalance, bank.createdOn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateBalance(int bankId, int relativeAmount) {
        if (bankId == BOT_BANK_ACCOUNT.id || relativeAmount == 0) {
            return;
        }
        try {
            WebDb.get().query("UPDATE banks SET current_balance = current_balance + ? WHERE id = ?", relativeAmount, bankId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update(OBank bank) {
        if (bank.id == 0) {
            insert(bank);
            return;
        }
        try {
            WebDb.get().query(
                    "UPDATE  banks SET current_balance = ? WHERE id = ? ",
                    bank.currentBalance, bank.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init(String botId, String botName) {
        OUser user = CUser.findBy(botId);
        if (user.id == 0 || botId.equals(user.name) || user.name.isEmpty()) {
            user.name = botName;
            user.discord_id = botId;
            CUser.update(user);
        }
        BOT_BANK_ACCOUNT = findBy(botId);
        BOT_BANK_ACCOUNT.currentBalance = Integer.MAX_VALUE;
        update(BOT_BANK_ACCOUNT);
    }
}
