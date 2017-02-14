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

package discordbot.command.fun;

import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * !roll
 * return a random number
 */
public class RollCommand extends AbstractCommand {
    Random rng;
    Pattern dice = Pattern.compile("(\\d+)d(\\d+)\\+?(\\d+)?");

    public RollCommand() {
        super();
        rng = new Random();
    }

    @Override
    public String getDescription() {
        return "if you ever need a random number";
    }

    @Override
    public String getCommand() {
        return "roll";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "roll               //random number 1-6",
                "roll <max>         //random number 1-<max>",
                "roll <min> <max>   //random number <min>-<max>",
                "roll XdY           //eg. 2d5 rolls 2 dice of 1-5 and returns the sum",
                "roll XdY+z         //eg. 2d5+2 rolls 2 dice of 1-5 and returns the sum plus 2",
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{
                "dice",
                "rng"
        };
    }

    public String multiDice(int dices, int sides, int bonus) {
        String text = String.format("Rolling %s x %s-sided dice: ", dices, sides);
        int total = 0;
        for (int i = 0; i < dices; i++) {
            int roll = rng.nextInt(sides) + 1;
            text += " " + roll;
            total += roll;
        }
        if (bonus != 0) {
            text += " adding " + bonus;
            total += bonus;
        }
        return text + " Total: **" + total + "**";
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        int min = 1, max = 6, max_dice = 40, min_sides = 2;
        if (args.length == 1) {
            Matcher match = dice.matcher(args[0]);
            if (match.find()) {
                int dice = Misc.parseInt(match.group(1),1);
                int sides = Misc.parseInt(match.group(2),6);
                int bonus = 0;
                if (dice > max_dice) {
                    return Template.get("command_roll_dice_count", max_dice);
                }
                if (dice < 1) {
                    return Template.get("command_roll_no_dice");
                }
                if (sides < min_sides) {
                    return Template.get("command_roll_side_count", min_sides);
                }
                if (match.group(3) != null && !"null".equals(match.group(3))) {
                    bonus = Misc.parseInt("" + match.group(3),0);
                }
                return multiDice(dice, sides, bonus);
            }
            try {
                max = Integer.parseInt(args[0]);
            } catch (Exception e) {
                return "Thats not a valid number";
            }
            if (max < 2) {
                return "Needs to have at least 2 sides";
            }
        } else if (args.length == 2) {
            try {
                min = Integer.parseInt(args[0]);
                max = Integer.parseInt(args[1]);
            } catch (Exception e) {
                return "Thats not a valid number";
            }
            if (min >= max) {
                return "Max needs to be higher than min!";
            }
            if (max <= 2 || min <= 0) {
                return "Min needs to be at least 0 and Max needs to be at least 2";
            }

        }
        return String.format("Rolling between *%s* and *%s*. Result: **%s**", min, max, rng.nextInt(1 + max - min) + min);
    }
}