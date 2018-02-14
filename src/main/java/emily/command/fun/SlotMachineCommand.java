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

package emily.command.fun;

import emily.command.CooldownScope;
import emily.command.ICommandCooldown;
import emily.core.AbstractCommand;
import emily.db.controllers.CBanks;
import emily.db.model.OBank;
import emily.games.SlotMachine;
import emily.games.slotmachine.Slot;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.templates.Template;
import emily.templates.Templates;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.Future;

/**
 * Created on 23-8-2016
 */
public class SlotMachineCommand extends AbstractCommand implements ICommandCooldown {

    private final long SPIN_INTERVAL = 2000L;
    private final int MAX_BET = 1000;

    public SlotMachineCommand() {
        super();
    }

    @Override
    public long getCooldownDuration() {
        return 1L;
    }

    @Override
    public CooldownScope getScope() {
        return CooldownScope.USER;
    }

    @Override
    public String getDescription() {
        return "Feeling lucky? try the slotmachine! You might just win a hand full of air!";
    }

    @Override
    public String getCommand() {
        return "slot";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "slot              //spin the slotmachine",
                "slot [cookies]    //play for real cookies where [cookies] is the amount of cookies you bet",
                "slot info         //info about payout"
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (args.length == 0 || args.length >= 1 && !args[0].equals("info")) {
            final int betAmount;
            if (args.length > 0 && args[0].matches("\\d+")) {
                betAmount = Math.min(Misc.parseInt(args[0], 0), MAX_BET);
            } else {
                betAmount = 0;
            }
            if (betAmount > 0) {
                OBank bank = CBanks.findBy(author.getId());
                if (bank.currentBalance < betAmount) {
                    return Templates.gamble_insufficient_funds.format(betAmount, BotConfig.ECONOMY_CURRENCY_ICON);
                }
                bank.transferTo(CBanks.getBotAccount(), betAmount, "slot machine");
            }
            final SlotMachine slotMachine = new SlotMachine();
            bot.queue.add(channel.sendMessage(slotMachine.toString()), message -> {
                final Future<?>[] f = {null};
                f[0] = bot.scheduleRepeat(() -> {
                    try {
                        if (slotMachine.gameInProgress()) {
                            slotMachine.spin();
                        }
                        String gameResult;
                        if (!slotMachine.gameInProgress()) {
                            int winMulti = slotMachine.getWinMultiplier();
                            if (winMulti > 0) {
                                if (betAmount > 0) {
                                    gameResult = String.format("%s %s - Thats %s %s for you!", slotMachine.getWinSlotTimes(), slotMachine.getWinSlot().getEmote(), betAmount * winMulti, BotConfig.ECONOMY_CURRENCY_ICON);
                                    CBanks.getBotAccount().transferTo(CBanks.findBy(author.getId()), betAmount * winMulti, "slot winnings!");
                                } else {
                                    gameResult = "You rolled " + slotMachine.getWinSlotTimes() + " **" + slotMachine.getWinSlot().getEmote() + "** and won **nothing**";
                                }
                            } else {
                                gameResult = Templates.gamble_ai_lose.format();
                            }
                            bot.queue.add(message.editMessage(slotMachine.toString() + "\n" + gameResult));
                            f[0].cancel(false);
                        } else {
                            bot.queue.add(message.editMessage(slotMachine.toString()));
                        }
                    } catch (Exception e) {
                        bot.getContainer().reportError(e, "slotmachine", author.getId(), "channel", ((TextChannel) channel).getAsMention(), bot);
                        f[0].cancel(false);
                    }
                }, 1000L, SPIN_INTERVAL);
            });
        } else {
            StringBuilder ret = new StringBuilder("The slotmachine!" + "\n");
            ret.append("payout is as follows: " + "\n");
            for (Slot s : Slot.values()) {
                ret.append(String.format("%1$s %1$s %1$s = %2$s" + "\n", s.getEmote(), s.getTriplePayout()));
            }
            ret.append("type **slot play** to give it a shot!");
            return ret.toString();
        }
        return "";
    }
}