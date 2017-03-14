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

package emily.modules.github;

import com.google.gson.Gson;
import emily.modules.github.pojo.RepositoryCommit;
import emily.util.HttpHelper;

import java.util.Calendar;

public class GitHub {
    private static final Gson gson = new Gson();

    /**
     * Retrieves a list of changes since timestamp
     *
     * @param username   the github username
     * @param repository the repository name
     * @param timestamp  the starting timestamp
     * @return a list of commits since timestamp
     */
    public static RepositoryCommit[] getChangesSinceTimestamp(String username, String repository, long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String response = HttpHelper.doRequest(GithubConstants.getCommitEndPoint(username, repository, timestamp));
        return gson.fromJson(response, RepositoryCommit[].class);
    }
}
