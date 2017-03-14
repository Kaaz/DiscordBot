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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * !catfact
 * gives you a random cat fact
 */
public class CatFactCommand extends AbstractCommand {
    public CatFactCommand() {
        super();
    }

    public static String getCatFact() {
        try {
            URL loginurl = new URL("http://catfacts-api.appspot.com/api/facts");
            URLConnection yc = loginurl.openConnection();
            yc.setConnectTimeout(10 * 1000);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine = in.readLine();
            JsonParser parser = new JsonParser();
            JsonObject array = parser.parse(inputLine).getAsJsonObject();
            return ":cat:  " + array.get("facts").getAsString();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "Cat facts!";
    }

    @Override
    public String getCommand() {
        return "catfact";
    }

    @Override
    public String[] getUsage() {
        return new String[]{};
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        String catFact = getCatFact();
        if (catFact != null) {
            return StringEscapeUtils.unescapeHtml4(catFact);
        }
        return Template.get("command_catfact_not_today");
    }
}
