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
import emily.handler.CommandHandler;
import emily.handler.Template;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

/**
 * !joke
 * gives you a random chuck norris joke with chuck norris replaced by <@user>
 */
public class JokeCommand extends AbstractCommand {
    public JokeCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "An attempt to be funny";
    }

    @Override
    public String getCommand() {
        return "joke";
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        bot.out.sendAsyncMessage(channel, Template.get("command_joke_wait"), message -> {
            String joketxt = "";
            if (new Random().nextInt(100) < 80) {
                joketxt = CommandHandler.getCommand("reddit").execute(bot, new String[]{"jokes"}, channel, author, null);
            } else {
                try {
                    joketxt = getJokeFromWeb(URLEncoder.encode(author.getName(), "UTF-8"));
                } catch (UnsupportedEncodingException ignored) {
                }
            }
            if (joketxt != null && !joketxt.isEmpty()) {
                bot.out.editAsync(message, StringEscapeUtils.unescapeHtml4(joketxt.replace(author.getName(), "<@" + author.getId() + ">")));
            } else {
                bot.out.editAsync(message, Template.get("command_joke_not_today"));
            }
        });
        return "";
    }

    private String getJokeFromWeb(String username) {
        try {
            URL loginurl = new URL("http://api.icndb.com/jokes/random?firstName=&lastName=" + username);
            URLConnection yc = loginurl.openConnection();
            yc.setConnectTimeout(10 * 1000);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine = in.readLine();
            JsonParser parser = new JsonParser();
            JsonObject array = parser.parse(inputLine).getAsJsonObject();
            return array.get("value").getAsJsonObject().get("joke").getAsString();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}