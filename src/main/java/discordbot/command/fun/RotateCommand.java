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
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016-09-14.
 */
public class RotateCommand extends AbstractCommand {

    private static final Map<String, String> charMap;

    static {
        charMap = new HashMap<>();
        charMap.put("a", "\u0250");
        charMap.put("b", "q");
        charMap.put("c", "\u0254");
        charMap.put("d", "p");
        charMap.put("e", "\u01DD");
        charMap.put("f", "\u025F");
        charMap.put("g", "\u0183");
        charMap.put("h", "\u0265");
        charMap.put("i", "\u1D09");
        charMap.put("j", "\u027E");
        charMap.put("k", "\u029E");
        charMap.put("m", "\u026F");
        charMap.put("n", "u");
        charMap.put("r", "\u0279");
        charMap.put("t", "\u0287");
        charMap.put("v", "\u028C");
        charMap.put("w", "\u028D");
        charMap.put("y", "\u028E");
        charMap.put("A", "\u2200");
        charMap.put("C", "\u0186");
        charMap.put("E", "\u018E");
        charMap.put("F", "\u2132");
        charMap.put("G", "\u05E4");
        charMap.put("H", "H");
        charMap.put("I", "I");
        charMap.put("J", "\u017F");
        charMap.put("L", "\u02E5");
        charMap.put("M", "W");
        charMap.put("N", "N");
        charMap.put("P", "\u0500");
        charMap.put("T", "\u2534");
        charMap.put("U", "\u2229");
        charMap.put("V", "\u039B");
        charMap.put("Y", "\u2144");
        charMap.put("1", "\u0196");
        charMap.put("2", "\u1105");
        charMap.put("3", "\u0190");
        charMap.put("4", "\u3123");
        charMap.put("5", "\u03DB");
        charMap.put("6", "9");
        charMap.put("7", "\u3125");
        charMap.put("8", "8");
        charMap.put("9", "6");
        charMap.put("0", "0");
        charMap.put(".", "\u02D9");
        charMap.put(",", " '");
        charMap.put("'", ",");
        charMap.put("\"", ",,");
        charMap.put("`", ",");
        charMap.put("?", "\u00BF");
        charMap.put("!", "\u00A1");
        charMap.put("[", "]");
        charMap.put("]", "[");
        charMap.put("(", ")");
        charMap.put(")", "(");
        charMap.put("{", "}");
        charMap.put("}", "{");
        charMap.put("<", ">");
        charMap.put(">", "<");
        charMap.put("&", "\u214B");
        charMap.put("_", "\u203E");
        charMap.put("\u2234", "\u2235");
        charMap.put("\u2045", "\u2046");
    }

    public RotateCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Rotate text!";
    }

    @Override
    public String getCommand() {
        return "rotate";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "rotate <text..> "
        };
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        List<String> inputArray = new ArrayList<>();
        String output = "";
        for (String arg : args) {
            inputArray.add(" ");
            Collections.addAll(inputArray, arg.split(""));
        }
        if (inputArray.size() <= 3) {
            return Template.get("command_rotate_too_short");
        }
        for (int i = inputArray.size() - 1; i >= 0; i--) {
            if (inputArray.get(i) == null) {
                continue;
            }
            if (charMap.containsKey(inputArray.get(i))) {
                output += charMap.get(inputArray.get(i));
            } else {
                output += inputArray.get(i);
            }
        }
        return output;
    }
}