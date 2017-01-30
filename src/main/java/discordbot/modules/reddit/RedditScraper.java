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

package discordbot.modules.reddit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import discordbot.modules.reddit.pojo.Comment;
import discordbot.modules.reddit.pojo.CommentData;
import discordbot.modules.reddit.pojo.InitialData;
import discordbot.modules.reddit.pojo.InitialDataComment;
import discordbot.modules.reddit.pojo.Post;
import discordbot.util.HttpHelper;

import java.util.ArrayList;
import java.util.List;

public class RedditScraper {
    private final static Gson gson = new GsonBuilder().
            registerTypeAdapter(CommentData.class, new CommentDataDeserializer()).
            excludeFieldsWithoutExposeAnnotation().
            create();

    public static List<Post> search(String subreddit, String arguments) {

        String response = HttpHelper.doRequest(RedditConstants.URL + RedditConstants.SUBREDDIT_INDICATOR + subreddit + RedditConstants.SEARCH_PAGE + arguments);
        InitialData listing = gson.fromJson(response, InitialData.class);
        if (listing.data.children != null) {
            return listing.data.children;
        }
        return new ArrayList<>();
    }

    public static List<Post> getDailyTop(String subreddit) {

        String response = HttpHelper.doRequest(RedditConstants.URL + RedditConstants.SUBREDDIT_INDICATOR + subreddit + "/top.json?sort=top&t=day&limit=100");
        InitialData listing = gson.fromJson(response, InitialData.class);
        if (listing.data != null && listing.data.children != null) {
            return listing.data.children;
        }
        return new ArrayList<>();
    }

    public static List<Comment> getComments(String id) {
        String response = HttpHelper.doRequest(RedditConstants.URL + "comments/" + id + ".json");
        List<InitialDataComment> initialData = gson.fromJson(response, new TypeToken<ArrayList<InitialDataComment>>() {
        }.getType());
        List<Comment> commentList = new ArrayList<>();
        for (InitialDataComment initialDataComment : initialData) {
            commentList.addAll(initialDataComment.data.children);
        }
        return commentList;
    }
}
