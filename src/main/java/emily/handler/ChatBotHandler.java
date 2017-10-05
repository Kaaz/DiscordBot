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
import emily.modules.cleverbotio.CleverbotIO;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChatBotHandler {
    private final Map<String, ChatBotInstance> sessions;

    public ChatBotHandler() {
        sessions = new ConcurrentHashMap<>();
    }

    private CleverbotIO getSession(String nick) {
        return new CleverbotIO(BotConfig.CLEVERBOT_IO_USER, BotConfig.CLEVERBOT_IO_KEY, nick);
    }

    public void cleanCache() {
        long deleteBefore = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30);
        Iterator<Map.Entry<String, ChatBotInstance>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ChatBotInstance> entry = iterator.next();
            if (entry.getValue().getLastInteraction() < deleteBefore) {
                sessions.remove(entry.getKey());
            }
        }
    }

    public String chat(String guildId, String input) {
        if (!sessions.containsKey(guildId)) {
            sessions.put(guildId, new ChatBotInstance(getSession(guildId)));
        }
        return sessions.get(guildId).chat(input);
    }

    private class ChatBotInstance {
        private long lastInteraction;
        private int failedAttempts = 0;
        private CleverbotIO botsession = null;

        ChatBotInstance(CleverbotIO session) {
            botsession = session;
        }

        public long getLastInteraction() {
            return lastInteraction;
        }

        public String chat(String input) {
            if (failedAttempts > 25) {
                return "";
            }
            try {
                failedAttempts = 0;
                lastInteraction = System.currentTimeMillis();
                String string;
                while (!acceptableMessage(string = new String(botsession.ask(input).getBytes("UTF-8"), "UTF-8"))){
                    Thread.sleep(250);
                }
                return string;
            } catch (Exception ignored) {
                failedAttempts++;
            }
            return "";
        }

    }

    private static final List<String> BANNED = Arrays.asList("discord.gg", "http", "clan", "server", "you");
    private static boolean acceptableMessage(String s){
        s = s.toLowerCase();
        if (Character.isLetter(s.charAt(0))) return false;
        for (String s1 : BANNED){
            if (s.contains(s1)) return false;
        }
        return true;
    }
}
