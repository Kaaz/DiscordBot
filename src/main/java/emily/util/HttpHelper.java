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
import emily.main.Config;

/**
 * Created on 8-9-2016
 */
public class HttpHelper {

    /**
     * @param url the url to request to
     * @return a string containing the response
     */
    public static String doRequest(String url) {
        try {
            return Unirest.get(url).header("User-Agent", Config.USER_AGENT).asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return "";
    }
}
