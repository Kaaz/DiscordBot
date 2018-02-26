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

package emily.handler;


import emily.main.BotConfig;
import emily.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.bots4j.wit.WitClient;
import org.bots4j.wit.beans.EntityMap;
import org.bots4j.wit.beans.GetIntentViaTextResponse;
import org.bots4j.wit.beans.Outcome;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChatBotHandler {
    private final Map<Long, ChatBotInstance> sessions;
    private final DiscordBot bot;

    public ChatBotHandler(DiscordBot bot) {
        this.bot = bot;
        sessions = new ConcurrentHashMap<>();
    }

    private WitClient createSession() {
        return new WitClient(BotConfig.WIT_AI_TOKEN);
    }

    public void cleanCache() {
        long deleteBefore = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30);
        Iterator<Map.Entry<Long, ChatBotInstance>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, ChatBotInstance> entry = iterator.next();
            if (entry.getValue().getLastInteraction() < deleteBefore) {
                sessions.remove(entry.getKey());
            }
        }
    }

    public String chat(long guildId, String input, MessageChannel channel) {
        if (!sessions.containsKey(guildId)) {
            sessions.put(guildId, new ChatBotInstance(createSession()));
        }
        return sessions.get(guildId).chat(input, channel);
    }

    private class ChatBotInstance {
        private long lastInteraction;
        private int failedAttempts = 0;
        private WitClient botsession;

        ChatBotInstance(WitClient session) {
            botsession = session;
        }

        public long getLastInteraction() {
            return lastInteraction;
        }

        public String chat(String input, MessageChannel channel) {
            if (failedAttempts > 25) {
                return "";
            }
            try {
                failedAttempts = 0;
                lastInteraction = System.currentTimeMillis();
                GetIntentViaTextResponse intent = botsession.getIntentViaText(input, null, null, null, null);
                if (intent.getOutcomes().isEmpty()) {
                    return "";
                }
                Outcome outcome = intent.getOutcomes().get(0);
                EntityMap entities = outcome.getEntities();
                for (Map.Entry<String, Object> stringObjectEntry : entities.entrySet()) {
                    System.out.println(stringObjectEntry.getKey() + " ++ " + stringObjectEntry.getValue());
                }
                String search = entities.firstEntityValue("search_query");
                ResponseCategory category = ResponseCategory.get(entities.firstEntityValue("intent"));
                switch (category){
                    case COMMANDHELP:
                        if(CommandHandler.commandExists(search)){
                            return CommandHandler.getCommand("help").execute(bot, new String[]{search}, channel, null, null);
                        }
                        return "No info for `"+search+"`";
                    case COMMANDEXECUTE:
                        search = search.replace(" ","");
                        if(CommandHandler.commandExists(search)){
                            return CommandHandler.getCommand(search).execute(bot, new String[]{}, channel, bot.getJda().getSelfUser(), null);
                        }
                        return "Cant find a command for `"+search+"`";
                }
                return String.format("category: %s; details: `%s`", category, search);
            } catch (Exception ignored) {
                failedAttempts++;
            }
            return "";
        }
    }
    enum ResponseCategory{
        COMMANDHELP("command-help"),
        COMMANDEXECUTE("command-execute"),
        UNKNOWN("?");

        private final String categoryname;

        ResponseCategory(String categoryname) {
            this.categoryname = categoryname;
        }

        public String getCategoryname() {
            return categoryname;
        }
        public static ResponseCategory get(String text){
            for (ResponseCategory responseCategory : values()) {
                if(responseCategory.getCategoryname().equals(text)){
                    return responseCategory;
                }
            }
            return UNKNOWN;
        }
    }
}
