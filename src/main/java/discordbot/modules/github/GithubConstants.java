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

package discordbot.modules.github;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 8-9-2016
 */
public class GithubConstants {

    public static final SimpleDateFormat githubDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final String ENDPOINT = "https://api.github.com/";

    //username, repository
    private static final String COMMIT_ENDPOINT = ENDPOINT + "repos/%s/%s/commits";

    public static String getCommitEndPoint(String user, String repository, long timestamp) {
        if (timestamp <= 0L) {
            return getCommitEndPoint(user, repository);
        }
        return String.format(COMMIT_ENDPOINT, user, repository) + "?since=" + githubDate.format(new Date(timestamp));
    }

    private static String getCommitEndPoint(String user, String repository) {
        return String.format(COMMIT_ENDPOINT, user, repository);
    }
}
