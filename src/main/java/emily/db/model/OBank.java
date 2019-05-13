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

package emily.db.model;

import emily.db.AbstractModel;
import emily.db.controllers.CBankTransactions;
import emily.db.controllers.CBanks;
import net.dv8tion.jda.core.entities.*;


import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * Created on 5-9-2016
 */
public class OBank extends AbstractModel {
    public int userId = 0;
    public int id = 0;
    public long currentBalance = 0L;
    public Timestamp createdOn = null;
    public long salary = 0L;

    public boolean transferTo(OBank target, int amount, String description) {
        if (id == 0) {
            return false;
        }
        if (id == target.id) {
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

    public void setSalary(OBank target, long salary){
        target.salary = salary;
        //CBanks.updateSalary(id, salary);
    }

    public boolean collectSalary(OBank user, long amount, TextChannel targetChannel, Guild currGuild){
        try{
            TimeUnit.SECONDS.sleep(10);

            String message = "Salary added to balance.";
            targetChannel.sendMessage(message).queueAfter(10, TimeUnit.SECONDS);
        } catch (Exception e){
            return false;
        }
        return true;
    }
}
