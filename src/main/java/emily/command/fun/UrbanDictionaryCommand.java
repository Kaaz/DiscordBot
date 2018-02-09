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

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import emily.core.AbstractCommand;
import emily.handler.Template;
import emily.main.DiscordBot;
import emily.util.Emojibet;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * !ud
 * urban dictionary
 */
public class UrbanDictionaryCommand extends AbstractCommand {
    public UrbanDictionaryCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "A veritable cornucopia of streetwise lingo";
    }

    @Override
    public String getCommand() {
        return "ud";
    }

    @Override
    public boolean isListed() {
        return true;
    }

    @Override
    public String[] getUsage() {
        return new String[]{"ud <anything>  //looks up what it means on urban dictionary"};
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (args.length == 0) {
            return Template.get("command_invalid_use");
        }
        String search = Joiner.on(" ").join(args);
        try {
            Future<HttpResponse<JsonNode>> future = Unirest.get("http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(search, "UTF-8")).asJsonAsync();
            HttpResponse<JsonNode> json = future.get(30, TimeUnit.SECONDS);
            JSONArray list = json.getBody().getObject().getJSONArray("list");
            if (list.length() == 0) {
                return Template.get("command_ud_no_results", search);
            }
            JSONObject item = list.getJSONObject(0);
            return String.format("Urban Dictionary " + "\n" + "\n"
                            + "Definition for **%s**: " + "\n"
                            + "```" + "\n"
                            + "%s" + "\n"
                            + "```" + "\n"
                            + "**example**: " + "\n"
                            + "%s" + "\n" + "\n"
                            + "_by %s (" + Emojibet.THUMBS_UP + "%s  " + Emojibet.THUMBS_DOWN + "%s)_"
                    , item.getString("word"), item.getString("definition"), item.getString("example"),
                    item.getString("author"), item.getInt("thumbs_up"), item.getInt("thumbs_down"));
        } catch (Exception ignored) {
            System.out.println(ignored.getMessage());
            ignored.printStackTrace();
        }
        return Template.get("command_ud_no_results", search);
    }
}