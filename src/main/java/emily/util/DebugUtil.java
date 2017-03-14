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

package emily.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.MessageChannel;

public class DebugUtil {
    /**
     * Handles the debug output + response
     *
     * @param channel the channel to send the messages to
     * @param output  the output to upload
     */
    public static void handleDebug(MessageChannel channel, String output) {
        channel.sendMessage("One moment, uploading results: ").queue(message -> {
            String result = DebugUtil.sendToHastebin(output);
            if (result == null) {
                message.editMessage("Uploading failed!").queue();
            } else {
                message.editMessage("Here you go: " + result).queue();
            }
        });

    }

    /**
     * attempts to send the message to hastebin
     *
     * @param message the message to send
     * @return the url or null
     */
    public static String sendToHastebin(String message) {
        try {
            return "http://hastebin.com/" + handleHastebin(message);
        } catch (UnirestException ignored) {
        }
        return null;
    }

    /**
     * dumps a string to hastebin
     *
     * @param message the text to send
     * @return key how to find it
     */
    private static String handleHastebin(String message) throws UnirestException {
        return Unirest.post("https://hastebin.com/documents").body(message).asJson().getBody().getObject().getString("key");
    }
}
