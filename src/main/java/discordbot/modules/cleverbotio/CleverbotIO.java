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

package discordbot.modules.cleverbotio;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.json.JSONObject;

public class CleverbotIO {
    private String user;
    private String key;
    private String session;

    public CleverbotIO(String user, String key, String nick) {
        this.user = user;
        this.key = key;
        this.session = nick;
        create();
    }

    public void create() {
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("user", user)
                .put("key", key)
                .put("nick", session);
        RequestBodyEntity post = Unirest.post("https://cleverbot.io/1.0/create").header("Content-Type", "application/json")
                .body(jsonOut.toString());


    }

    public String ask(String query) throws UnirestException {
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("user", user)
                .put("key", key)
                .put("nick", session)
                .put("text", query);
        RequestBodyEntity post = Unirest.post("https://cleverbot.io/1.0/ask").header("Content-Type", "application/json")
                .body(jsonOut.toString());
        JSONObject json = post.asJson().getBody().getObject();
        return json.getString("response");
    }
}
